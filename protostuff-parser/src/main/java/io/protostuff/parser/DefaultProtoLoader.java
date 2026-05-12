//========================================================================
//Copyright 2007-2010 David Yu dyuproject@gmail.com
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package io.protostuff.parser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import io.protostuff.parser.builder.ProtoBuilder;

/**
 * Default proto loader for imported protos.
 *
 * @author David Yu
 * @created May 16, 2010
 */
public class DefaultProtoLoader implements Proto.Loader {

	protected static final int ALL = 0, PROTO_PATH_ONLY = 1, PROTO_PATH_AND_CLASSPATH = 2;

	/**
	 * The default proto search strategy to use.
	 */
	public static final int DEFAULT_PROTO_SEARCH_STRATEGY = Integer.getInteger("proto_search_strategy", ALL);

	/**
	 * Standard JVM property.
	 * See https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
	 */
	public static final String PATH_SEPARATOR;
	public static final String PATH_SEPARATOR_PROPERTY = "path.separator";
	private static final String PATH_SEPARATOR_DEFAULT = ";";

	public static final DefaultProtoLoader DEFAULT_INSTANCE = new DefaultProtoLoader();

	private static final ArrayList<File> __protoLoadDirs = new ArrayList<>();

	static {
		String pathSeparatorProperty = System.getProperty(PATH_SEPARATOR_PROPERTY);
		if (pathSeparatorProperty == null) {
			pathSeparatorProperty = PATH_SEPARATOR_DEFAULT;
		}
		PATH_SEPARATOR = pathSeparatorProperty;

		String protoPath = System.getProperty("proto_path");
		if (protoPath != null) {
			StringTokenizer tokenizer = new StringTokenizer(protoPath, PATH_SEPARATOR);
			while (tokenizer.hasMoreTokens()) {
				String path = tokenizer.nextToken().trim();
				File dir = new File(path);
				if (dir.exists() && dir.isDirectory()) {
					__protoLoadDirs.add(dir);
				} else {
					System.err.println("warn: " + path + " not found.");
				}
			}
		}
	}

	public static DefaultProtoLoader getDefaultInstance() {
		return DEFAULT_INSTANCE;
	}

	protected final int protoSearchStrategy;

	public DefaultProtoLoader() {
		this(DEFAULT_PROTO_SEARCH_STRATEGY);
	}

	public DefaultProtoLoader(int protoSearchStrategy) {
		this.protoSearchStrategy = protoSearchStrategy;
	}

	@Override
	public Proto load(String path, ProtoBuilder importer) throws ProtoParserException {
		switch (protoSearchStrategy) {
			case ALL:
				return searchFromAll(path, importer);

			case PROTO_PATH_ONLY:
				return searchFromProtoPathOnly(path, importer);

			case PROTO_PATH_AND_CLASSPATH:
				return searchFromProtoPathAndClasspath(path, importer);

			default:
				return searchFromAll(path, importer);
		}
	}

	/**
	 * Search from proto_path only. For full protoc compatibility, use this.
	 * <p>
	 *
	 * <pre>
	 * Enable via:
	 * -Dproto_path=$path -Dproto_search_strategy=1
	 * </pre>
	 */
	protected Proto searchFromProtoPathOnly(String path, ProtoBuilder importer) throws ProtoParserException {
		// proto_path
		File protoFile;
		for (File dir : __protoLoadDirs) {
			if ((protoFile = new File(dir, path)).exists()) {
				return ProtoUtil.parseProto(protoFile, this);
			}
		}

		throw new IllegalStateException("Imported proto " + path + " not found. (" + importer.getSource() + ")");
	}

	/**
	 * Search from proto_path and classpath (in that order).
	 * <p>
	 *
	 * <pre>
	 * Enable via:
	 * -Dproto_path=$path -Dproto_search_strategy=2
	 * </pre>
	 */
	protected Proto searchFromProtoPathAndClasspath(String path, ProtoBuilder importer) throws ProtoParserException {
		// proto_path
		File protoFile;
		for (File dir : __protoLoadDirs) {
			if ((protoFile = new File(dir, path)).exists()) {
				return ProtoUtil.parseProto(protoFile, this);
			}
		}

		// classpath
		Proto protoFromOtherResource = loadFromOtherResource(path);
		if (protoFromOtherResource == null) {
			throw new IllegalStateException("Imported proto " + path + " not found. (" + importer.getSource() + ")");
		}

		return protoFromOtherResource;
	}

	/**
	 * Search from every possible resource. Also loads from a remote url (if path starts with http://).
	 * <p>
	 *
	 * <pre>
	 * Search order is:
	 * 1. relative path
	 * 2. proto_path
	 * 3. classpath
	 * </pre>
	 */
	protected Proto searchFromAll(String path, ProtoBuilder importer) throws ProtoParserException {
		if (path.startsWith("http://")) {
			try {
				URL url = new URL(path);
				return ProtoUtil.parseProto(url, this);
			} catch (MalformedURLException e) {
				throw new ProtoParserException("Failed to parse " + importer.getSource(), e);
			}
		}

		File protoFile, importerFile = new File(importer.getSource());
		// check if its relative to its importer's dir.
		protoFile = new File(importerFile.getAbsoluteFile().getParentFile(), path);
		if (!protoFile.exists() && !(protoFile = new File(path)).exists()) {
			// check if its relative to its importer's package base dir.
			File baseDir = getBaseDirFromPackagePath(importer);
			if (baseDir != null) {
				protoFile = new File(baseDir, path);
			}
		}

		if (!protoFile.exists() && !__protoLoadDirs.isEmpty()) {
			// check from the "proto_path" provided as a system property
			for (File dir : __protoLoadDirs) {
				if ((protoFile = new File(dir, path)).exists()) {
					// found
					break;
				}
			}
		}

		if (protoFile.exists()) {
			return ProtoUtil.parseProto(protoFile, this);
		}

		// last resort (defaults to classpath lookup).
		Proto protoFromOtherResource = loadFromOtherResource(path);
		if (protoFromOtherResource == null) {
			throw new IllegalStateException("Imported proto " + path + " not found. (" + importer.getSource() + ")");
		}

		return protoFromOtherResource;
	}

	static File getBaseDirFromPackagePath(ProtoBuilder importer) {
		String importerPkg = importer.getPackageName();
		// the imports are declared before the package
		if (importerPkg == null) {
			return null;
		}

		File baseDir = new File(importer.getSource()).getAbsoluteFile().getParentFile();

		// up one level if package contains a dot.
		for (int i = 0; (i = importerPkg.indexOf('.', i)) != -1; i++) {
			baseDir = baseDir.getParentFile();
		}

		return baseDir;
	}

	protected Proto loadFromOtherResource(String path) {
		// defaults to lookup from classpath.
		URL resource = getResource(path, DefaultProtoLoader.class);
		return resource == null ? null : ProtoUtil.parseProto(resource, this);
	}

	/**
	 * Loads a proto from the classpath.
	 */
	public static Proto loadFromClasspath(String path) {
		URL resource = getResource(path, DefaultProtoLoader.class);
		if (resource == null) {
			return null;
		}

		return ProtoUtil.parseProto(resource);
	}

	/**
	 * Loads a {@link URL} resource from the classloader; If not found, the classloader of the {@code context} class
	 * specified will be used.
	 */
	public static URL getResource(String resource, Class<?> context) {
		return getResource(resource, context, false);
	}

	/**
	 * Loads a {@link URL} resource from the classloader; If not found, the classloader of the {@code context} class
	 * specified will be used. If the flag {@code checkParent} is true, the classloader's parent is included in the
	 * lookup.
	 */
	public static URL getResource(String resource, Class<?> context, boolean checkParent) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
		if (url != null) {
			return url;
		}

		if (context != null) {
			ClassLoader loader = context.getClassLoader();
			while (loader != null) {
				url = loader.getResource(resource);
				if (url != null) {
					return url;
				}
				loader = checkParent ? loader.getParent() : null;
			}
		}

		return ClassLoader.getSystemResource(resource);
	}

}
