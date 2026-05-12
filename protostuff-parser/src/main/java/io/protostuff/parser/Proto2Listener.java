package io.protostuff.parser;

import java.util.List;
import java.util.Stack;
import java.util.StringJoiner;

import org.antlr.v4.runtime.tree.TerminalNode;

import io.protostuff.parser.Field.Modifier;
import io.protostuff.parser.Proto2Parser.ConstantContext;
import io.protostuff.parser.Proto2Parser.EnumFieldContext;
import io.protostuff.parser.Proto2Parser.EnumStmtContext;
import io.protostuff.parser.Proto2Parser.ExtendContext;
import io.protostuff.parser.Proto2Parser.ExtentionsContext;
import io.protostuff.parser.Proto2Parser.FieldContext;
import io.protostuff.parser.Proto2Parser.FullIdContext;
import io.protostuff.parser.Proto2Parser.GroupContext;
import io.protostuff.parser.Proto2Parser.IdentContext;
import io.protostuff.parser.Proto2Parser.ImportStmtContext;
import io.protostuff.parser.Proto2Parser.LabelContext;
import io.protostuff.parser.Proto2Parser.MessageStmtContext;
import io.protostuff.parser.Proto2Parser.OptNameContext;
import io.protostuff.parser.Proto2Parser.OptionContext;
import io.protostuff.parser.Proto2Parser.PackageStmtContext;
import io.protostuff.parser.Proto2Parser.RangeContext;
import io.protostuff.parser.Proto2Parser.RpcBodyContext;
import io.protostuff.parser.Proto2Parser.RpcContext;
import io.protostuff.parser.Proto2Parser.ServiceStmtContext;
import io.protostuff.parser.Proto2Parser.TypeContext;
import io.protostuff.parser.Proto2Parser.TypeMapContext;
import io.protostuff.parser.Proto2Parser.TypeScalarContext;
import io.protostuff.parser.builder.AbstractBuilder;
import io.protostuff.parser.builder.EnumBuilder;
import io.protostuff.parser.builder.EnumBuilder.EnumFieldBuilder;
import io.protostuff.parser.builder.FieldBuilder;
import io.protostuff.parser.builder.GroupBuilder;
import io.protostuff.parser.builder.HasFieldBuilder;
import io.protostuff.parser.builder.HasMessageBuilder;
import io.protostuff.parser.builder.MessageBuilder;
import io.protostuff.parser.builder.ProtoBuilder;
import io.protostuff.parser.builder.RangesBuilder;
import io.protostuff.parser.builder.RpcBuilder;
import io.protostuff.parser.builder.ServiceBuilder;
import io.protostuff.parser.builder.WithModifier;
import io.protostuff.parser.builder.WithName;

public class Proto2Listener extends Proto2ParserBaseListener {

	private final Stack<Object> stack;

	public Proto2Listener(ProtoBuilder proto) {
		stack = new Stack<>();
		stack.push(proto);
	}

	private static Number parseNumber(String n) {
		if ("+inf".equals(n) || "inf".equals(n))
			return Double.POSITIVE_INFINITY;
		if ("-inf".equals(n))
			return Double.NEGATIVE_INFINITY;
		if ("nan".equals(n))
			return Double.NaN;
		try {
			return Long.parseLong(n);
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			return Double.parseDouble(n);
		}
	}

	@Override
	public void enterFullId(FullIdContext ctx) {
		stack.push(new FullId());
	}

	@Override
	public void exitIdent(IdentContext ctx) {
		((WithName<?>) stack.peek()).name(ctx.getText());
	}

	@Override
	public void exitFullId(FullIdContext ctx) {
		stack.push(stack.pop().toString());
	}

	@Override
	public void exitImportStmt(ImportStmtContext ctx) {
		if (ctx.WEAK() != null)
			throw new IllegalStateException("import weak not supported");
		if (ctx.PUBLIC() != null)
			throw new IllegalStateException("import public not supported");
		((ProtoBuilder) stack.peek()).importProto(TextFormat.unescapeQuoted(ctx.STRING_LITERAL().getText()));
	}

	@Override
	public void exitPackageStmt(PackageStmtContext ctx) {
		String lastId = (String) stack.pop();
		((ProtoBuilder) stack.peek()).setPackageName(lastId);
	}

	@Override
	public void exitConstant(ConstantContext ctx) {
		if (ctx.STRING_LITERAL() != null)
			stack.push(TextFormat.unescapeQuoted(ctx.STRING_LITERAL().getText()));
		else if (ctx.FALSE() != null)
			stack.push(false);
		else if (ctx.TRUE() != null)
			stack.push(true);
		else if (ctx.NUMBER() != null)
			stack.push(parseNumber(ctx.NUMBER().getText()));
		else if (ctx.OCTAL() != null) {
			String s = ctx.OCTAL().getText();
			char c = s.charAt(0);
			long l = Long.parseUnsignedLong(s.substring(c == '-' || c == '+' ? 2 : 1), 8);
			stack.push(c == '-' ? -l : l);
		} else if (ctx.HEX() != null) {
			String s = ctx.HEX().getText();
			char c = s.charAt(0);
			long l = Long.parseUnsignedLong(s.substring(c == '-' || c == '+' ? 3 : 2), 16);
			stack.push(c == '-' ? -l : l);
		}
	}

	@Override
	public void exitOptName(OptNameContext ctx) {
		StringBuilder sb = new StringBuilder();
		String after = null;
		if (ctx.DOT() != null)
			after = (String) stack.pop();

		if (ctx.RPAREN() != null)
			sb.append('(');
		sb.append(stack.pop());
		if (ctx.RPAREN() != null)
			sb.append(')');
		if (after != null)
			sb.append('.').append(after);
		stack.push(sb.toString());
	}

	@Override
	public void exitOption(OptionContext ctx) {
		if (ctx.OPT_FOR() != null) {
			AbstractBuilder o = (AbstractBuilder) stack.peek();
			if (ctx.LITE_RUNTIME() != null)
				o.putOption("optimize_for", "LITE_RUNTIME");
			else if (ctx.CODE_SIZE() != null)
				o.putOption("optimize_for", "CODE_SIZE");
			else
				o.putOption("optimize_for", "SPEED");
			return;
		}

		Object value = stack.pop();
		String name = (String) stack.pop();
		((AbstractBuilder) stack.peek()).putOption(name, value);
	}

	@Override
	public void enterMessageStmt(MessageStmtContext ctx) {
		System.out.println("enterMessage " + stack);
		stack.push(((HasMessageBuilder<?>) stack.peek()).newMessage());
	}

	@Override
	public void exitMessageStmt(MessageStmtContext ctx) {
		System.out.println("exitMessage " + stack);
		stack.pop();
	}

	@Override
	public void enterField(FieldContext ctx) {
		stack.push(((HasFieldBuilder) stack.peek()).newField());
	}

	@Override
	public void exitLabel(LabelContext ctx) {
		Modifier mod;
		if (ctx.REPEATED() != null)
			mod = Modifier.REPEATED;
		else if (ctx.REQUIRED() != null)
			mod = Modifier.REQUIRED;
		else
			mod = Modifier.OPTIONAL;
		((WithModifier<?>) stack.peek()).modifier(mod);
	}

	@Override
	public void exitTypeScalar(TypeScalarContext ctx) {
		stack.push(ctx.getText());
	}

	@Override
	public void exitTypeMap(TypeMapContext ctx) {
		stack.push(ctx.getText());
	}

	@Override
	public void exitType(TypeContext ctx) {
		String s = (String) stack.pop();
		((FieldBuilder) stack.peek()).type(s);
	}

	@Override
	public void exitField(FieldContext ctx) {
		((FieldBuilder) stack.pop()).number(Integer.parseInt(ctx.NUMBER().getText()));
	}

	@Override
	public void enterEnumStmt(EnumStmtContext ctx) {
		stack.push(((HasMessageBuilder<?>) stack.peek()).newEnumBuilder());
	}

	@Override
	public void exitEnumStmt(EnumStmtContext ctx) {
		System.out.println("exitEnum " + stack);
		stack.pop();
	}

	@Override
	public void enterEnumField(EnumFieldContext ctx) {
		stack.push(((EnumBuilder) stack.peek()).newField());
	}

	@Override
	public void exitEnumField(EnumFieldContext ctx) {
		EnumFieldBuilder builder = (EnumFieldBuilder) stack.pop();
		builder.number(Integer.parseInt(ctx.NUMBER().getText()));
	}

	@Override
	public void enterGroup(GroupContext ctx) {
		System.out.println("enterGroup " + stack);
		stack.push(((HasFieldBuilder) stack.peek()).newGroup());
	}

	@Override
	public void exitGroup(GroupContext ctx) {
		System.out.println("exitGroup " + stack);
		((GroupBuilder) stack.pop()).number(Integer.parseInt(ctx.NUMBER().getText()));
	}

	@Override
	public void enterExtend(ExtendContext ctx) {
		stack.push(((HasMessageBuilder<?>) stack.peek()).newExtend());
	}

	@Override
	public void exitExtend(ExtendContext ctx) {
		stack.pop();
	}

	@Override
	public void enterServiceStmt(ServiceStmtContext ctx) {
		stack.push(((ProtoBuilder) stack.peek()).newService());
	}

	@Override
	public void exitServiceStmt(ServiceStmtContext ctx) {
		stack.pop();
	}

	@Override
	public void enterRpc(RpcContext ctx) {
		stack.push(((ServiceBuilder) stack.peek()).newRpc());
	}

	@Override
	public void enterRpcBody(RpcBodyContext ctx) {
		String rtype = (String) stack.pop();
		String atype = (String) stack.pop();
		((RpcBuilder) stack.peek()).retType(rtype).argType(atype);
	}

	@Override
	public void exitRpc(RpcContext ctx) {
		stack.pop();
	}

	@Override
	public void enterExtentions(ExtentionsContext ctx) {
		stack.push(((MessageBuilder) stack.peek()).extensions());
	}

	@Override
	public void exitExtentions(ExtentionsContext ctx) {
		stack.pop();
	}

	@Override
	public void exitRange(RangeContext ctx) {
		List<TerminalNode> number = ctx.NUMBER();
		int s = parseNumber(number.get(0).getText()).intValue();
		int e = s;
		if (ctx.MAX() != null)
			e = 536870911;
		else if (number.size() > 1)
			e = parseNumber(number.get(1).getText()).intValue();
		((RangesBuilder) stack.peek()).addRange(s, e);
	}

	private static class FullId implements WithName<FullId> {
		private final StringJoiner sj = new StringJoiner(".");

		@Override
		public String name() {
			return sj.toString();
		}

		@Override
		public FullId name(String name) {
			sj.add(name);
			return this;
		}

		@Override
		public String toString() {
			return sj.toString();
		}

	}
}
