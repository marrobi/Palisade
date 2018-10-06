package uk.gov.gchq.palisade.policy.rule;

import org.junit.Test;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;

import java.nio.charset.Charset;

public class SerialiseRuleTest {

    @Test
    public void testRuleSerialisation() {
//        SerialisableRule<String> rule = new SerialisableRule<>("PassThrough", "return record;", "T");
//        SerialisableRule<String> rule = new SerialisableRule<>("Test2", "return !record.isEmpty() ? record : \"unknown\";", String.class.getCanonicalName());
        SerialisableRule<String> rule = new SerialisableRule<>("UserHasSensitiveClearance",
                "if(user.getAuths().contains(\"Sensitive\")) { " +
                        "return record; " +
                        "} else { " +
                        "return null; " +
                        "}",
                "T");

        byte[] jsonBytes = JSONSerialiser.serialise(rule);
        System.out.println(new String(jsonBytes, Charset.forName("UTF8")));

        SerialisableRule<String> newrule = JSONSerialiser.deserialise(jsonBytes, SerialisableRule.class);
        System.out.println(newrule.apply("test", new User().userId("test").auths("Sensitive"), null));
    }
}
