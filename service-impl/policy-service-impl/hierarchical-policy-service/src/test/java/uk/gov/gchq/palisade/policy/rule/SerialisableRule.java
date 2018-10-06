package uk.gov.gchq.palisade.policy.rule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.rule.Rule;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@JsonPropertyOrder(value = {"name", "ruleString", "typeClassName"}, alphabetic = true)
public class SerialisableRule<T> implements Rule<T>, Serializable {
    private String name;
    private String ruleString;
    private String typeClassName;
    @JsonIgnore
    Rule<T> rule;

    @JsonCreator
    public SerialisableRule(@JsonProperty("name") String name,
                            @JsonProperty("ruleString") String rule,
                            @JsonProperty("typeClassName") String typeClassName) {
        this.name = name;
        this.ruleString = rule;
        this.typeClassName = typeClassName;
    }

    @JsonIgnore
    private Rule<T> compileRuleClass() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        // Prepare source somehow.
        String source = "" +
                "package uk.gov.gchq.palisade.policy.rule;\n" +
                "import uk.gov.gchq.palisade.Context;\n" +
                "import uk.gov.gchq.palisade.User;\n" +
                "import uk.gov.gchq.palisade.rule.Rule;\n" +
                "public class " + name + "<T> implements Rule<" + typeClassName + "> { \n" +
                "public " + typeClassName + " apply(final " + typeClassName + " record, final User user, final Context context) { \n" +
                ruleString + "\n" +
                "}\n}";

        // Save source in .java file.
        File root = new File("/tmp/palisadeRule");
        File sourceFile = new File(root, "uk/gov/gchq/palisade/policy/rule/" +name+".java");
        sourceFile.getParentFile().mkdirs();
        Files.write(sourceFile.toPath(), source.getBytes(StandardCharsets.UTF_8));

        // Compile source file.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, sourceFile.getPath());
        // Load and instantiate compiled class.
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {root.toURI().toURL()});
        Class<?> cls = Class.forName("uk.gov.gchq.palisade.policy.rule." + name, true, classLoader);
        Rule<T> instance = (Rule<T>) cls.newInstance();
        return instance;
    }

    @Override
    public T apply(final T record, final User user, final Context context) {
        if (rule == null) {
            try {
                rule = compileRuleClass();
            } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return rule.apply(record, user, context);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getRuleString() {
        return ruleString;
    }

    public void setRuleString(final String ruleString) {
        this.ruleString = ruleString;
    }

    public String getTypeClassName() {
        return typeClassName;
    }

    public void setTypeClassName(final String typeClassName) {
        this.typeClassName = typeClassName;
    }
}
