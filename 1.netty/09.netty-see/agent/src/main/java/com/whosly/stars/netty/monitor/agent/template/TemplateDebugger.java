package com.whosly.stars.netty.monitor.agent.template;

import com.whosly.stars.netty.monitor.agent.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板调试器
 * 提供模板解析过程的详细调试信息和诊断功能
 * 
 * @author fengyang
 */
public class TemplateDebugger {
    
    private static final Logger logger = Logger.getLogger(TemplateDebugger.class);
    
    // 模板变量正则表达式
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * 调试模板解析过程
     * 
     * @param template 模板字符串
     * @param resolver 模板解析器
     * @param instance 对象实例
     * @return 调试信息
     */
    public static DebugInfo debugResolve(String template, TemplateResolver resolver, Object instance) {
        DebugInfo debugInfo = new DebugInfo(template);
        
        if (template == null || template.trim().isEmpty()) {
            debugInfo.addStep("模板为空或null，无需解析");
            return debugInfo;
        }
        
        debugInfo.addStep("开始解析模板: " + template);
        debugInfo.addStep("对象实例: " + (instance != null ? instance.getClass().getName() : "null"));
        
        // 分析模板中的变量
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        List<String> variables = new ArrayList<>();
        
        while (matcher.find()) {
            String variableExpression = matcher.group(1);
            variables.add(variableExpression);
            debugInfo.addVariable(variableExpression);
        }
        
        debugInfo.addStep("发现 " + variables.size() + " 个变量: " + variables);
        
        // 分析每个解析器
        List<VariableResolver> resolvers = resolver.getResolvers();
        debugInfo.addStep("可用解析器数量: " + resolvers.size());
        
        for (VariableResolver r : resolvers) {
            debugInfo.addStep("解析器: " + r.getName() + " (优先级: " + r.getPriority() + ")");
        }
        
        // 执行解析并记录过程
        String result = resolver.resolve(template, instance);
        debugInfo.setResult(result);
        debugInfo.addStep("解析完成: " + result);
        
        return debugInfo;
    }
    
    /**
     * 转储上下文信息
     * 
     * @param resolver 模板解析器
     * @return 上下文信息
     */
    public static ContextDump dumpContext(TemplateResolver resolver) {
        ContextDump dump = new ContextDump();
        
        // 解析器信息
        List<VariableResolver> resolvers = resolver.getResolvers();
        for (VariableResolver r : resolvers) {
            dump.addResolver(r.getName(), r.getPriority(), r.getClass().getName());
        }
        
        // 缓存信息
        dump.setCacheSize(resolver.getCacheSize());
        
        return dump;
    }
    
    /**
     * 验证模板并提供修复建议
     * 
     * @param template 模板字符串
     * @return 验证和建议信息
     */
    public static ValidationAdvice validateWithAdvice(String template) {
        ValidationAdvice advice = new ValidationAdvice();
        
        if (template == null) {
            advice.addError("模板不能为null");
            advice.addSuggestion("请提供有效的模板字符串");
            return advice;
        }
        
        if (template.trim().isEmpty()) {
            advice.addWarning("模板为空");
            advice.addSuggestion("考虑添加静态文本或变量表达式");
            return advice;
        }
        
        // 检查语法错误
        int openBraces = 0;
        int closeBraces = 0;
        boolean inVariable = false;
        
        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            
            if (c == '$' && i + 1 < template.length() && template.charAt(i + 1) == '{') {
                openBraces++;
                inVariable = true;
                i++; // 跳过 '{'
            } else if (c == '}' && inVariable) {
                closeBraces++;
                inVariable = false;
            }
        }
        
        if (openBraces != closeBraces) {
            advice.addError("大括号不匹配: 发现 " + openBraces + " 个 '${' 和 " + closeBraces + " 个 '}'");
            advice.addSuggestion("检查所有变量表达式是否正确闭合");
        }
        
        // 检查变量表达式
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String variableExpression = matcher.group(1);
            
            if (variableExpression.trim().isEmpty()) {
                advice.addError("发现空变量表达式: ${}");
                advice.addSuggestion("在 ${} 中添加变量名");
            }
            
            if (variableExpression.contains("${") || variableExpression.contains("}")) {
                advice.addError("变量表达式包含嵌套大括号: " + variableExpression);
                advice.addSuggestion("移除变量表达式中的额外大括号");
            }
            
            // 检查变量名格式
            String[] parts = variableExpression.split(":", 2);
            String variableName = parts[0].trim();
            
            if (variableName.isEmpty()) {
                advice.addError("变量名为空: " + variableExpression);
                advice.addSuggestion("在冒号前添加有效的变量名");
            }
            
            if (variableName.contains(" ")) {
                advice.addWarning("变量名包含空格: " + variableName);
                advice.addSuggestion("考虑使用下划线或驼峰命名法");
            }
        }
        
        if (advice.getErrors().isEmpty() && advice.getWarnings().isEmpty()) {
            advice.addInfo("模板语法正确");
        }
        
        return advice;
    }
    
    /**
     * 调试信息类
     */
    public static class DebugInfo {
        private final String template;
        private final List<String> steps = new ArrayList<>();
        private final List<String> variables = new ArrayList<>();
        private String result;
        
        public DebugInfo(String template) {
            this.template = template;
        }
        
        public void addStep(String step) {
            steps.add(step);
        }
        
        public void addVariable(String variable) {
            variables.add(variable);
        }
        
        public void setResult(String result) {
            this.result = result;
        }
        
        public String getTemplate() { return template; }
        public List<String> getSteps() { return new ArrayList<>(steps); }
        public List<String> getVariables() { return new ArrayList<>(variables); }
        public String getResult() { return result; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 模板解析调试信息 ===\n");
            sb.append("模板: ").append(template).append("\n");
            sb.append("变量: ").append(variables).append("\n");
            sb.append("结果: ").append(result).append("\n");
            sb.append("解析步骤:\n");
            for (int i = 0; i < steps.size(); i++) {
                sb.append("  ").append(i + 1).append(". ").append(steps.get(i)).append("\n");
            }
            return sb.toString();
        }
    }
    
    /**
     * 上下文转储类
     */
    public static class ContextDump {
        private final List<ResolverInfo> resolvers = new ArrayList<>();
        private int cacheSize;
        
        public void addResolver(String name, int priority, String className) {
            resolvers.add(new ResolverInfo(name, priority, className));
        }
        
        public void setCacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
        }
        
        public List<ResolverInfo> getResolvers() { return new ArrayList<>(resolvers); }
        public int getCacheSize() { return cacheSize; }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 模板解析器上下文转储 ===\n");
            sb.append("缓存大小: ").append(cacheSize).append("\n");
            sb.append("解析器列表:\n");
            for (ResolverInfo info : resolvers) {
                sb.append("  - ").append(info).append("\n");
            }
            return sb.toString();
        }
        
        public static class ResolverInfo {
            private final String name;
            private final int priority;
            private final String className;
            
            public ResolverInfo(String name, int priority, String className) {
                this.name = name;
                this.priority = priority;
                this.className = className;
            }
            
            public String getName() { return name; }
            public int getPriority() { return priority; }
            public String getClassName() { return className; }
            
            @Override
            public String toString() {
                return name + " (优先级: " + priority + ", 类: " + className + ")";
            }
        }
    }
    
    /**
     * 验证建议类
     */
    public static class ValidationAdvice {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> suggestions = new ArrayList<>();
        private final List<String> info = new ArrayList<>();
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
        public void addSuggestion(String suggestion) { suggestions.add(suggestion); }
        public void addInfo(String info) { this.info.add(info); }
        
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        public List<String> getSuggestions() { return new ArrayList<>(suggestions); }
        public List<String> getInfo() { return new ArrayList<>(info); }
        
        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 模板验证建议 ===\n");
            
            if (!errors.isEmpty()) {
                sb.append("错误:\n");
                for (String error : errors) {
                    sb.append("  ❌ ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("警告:\n");
                for (String warning : warnings) {
                    sb.append("  ⚠️ ").append(warning).append("\n");
                }
            }
            
            if (!suggestions.isEmpty()) {
                sb.append("建议:\n");
                for (String suggestion : suggestions) {
                    sb.append("  💡 ").append(suggestion).append("\n");
                }
            }
            
            if (!info.isEmpty()) {
                sb.append("信息:\n");
                for (String i : info) {
                    sb.append("  ℹ️ ").append(i).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
}