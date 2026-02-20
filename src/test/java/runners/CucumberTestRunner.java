package runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
    features = "src/test/resources/features",  // Ubicacion de archivos .feature
    glue = "stepdefinitions",                   // Paquete con las definiciones de pasos
    plugin = {
        "pretty",                               // Salida legible en consola
        "html:target/cucumber-reports.html",    // Reporte HTML
        "json:target/cucumber.json"             // Reporte JSON
    })
public class CucumberTestRunner {

}