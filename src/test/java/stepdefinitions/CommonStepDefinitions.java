package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import net.serenitybdd.core.Serenity;

// Pasos comunes a dos o mas escenarios, para evitar errores de repeticion
public class CommonStepDefinitions {

    //poner los pasos español, pues en los features se escribieron en español, y asi se mantiene la consistencia entre los archivos .feature y las definiciones de pasos. Ademas, es mas facil de entender para los testers que no estan familiarizados con el ingles tecnico.

    @Given("que estoy en la pagina de inicio de TheCubicle")
    public void iAmOnTheTheCubicleHomepage() {
        Serenity.getDriver().get("https://www.thecubicle.com/en-global"); // Abrir la página de inicio de TheCubicle
    }

    @When("selecciono {int} productos aleatorios de la pagina")
    public void iSelectRandomProductsFromThePage(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("La cantidad de productos debe ser mayor a 0");
        }
        // La seleccion real se ejecuta en los pasos de agregado al carrito de cada escenario.
    }
}
