package dk.dtu;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = { "pretty", "summary" }, features = "src/test/resources", glue = "dk.dtu.steps")
public class CucumberTest {
}