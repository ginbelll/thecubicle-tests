package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import net.serenitybdd.core.Serenity;
import steps.ProductSteps;

public class Test1StepDefinitions {

    private ProductSteps productSteps;

    // Inicializar ProductSteps solo cuando sea necesario para evitar problemas de inyeccion de dependencias en escenarios que no lo requieren
    private ProductSteps productSteps() {
        if (productSteps == null) {
            productSteps = new ProductSteps(Serenity.getDriver());
        }
        return productSteps;
    }

    @And("agrego cada producto al carrito")
    public void iAddEachProductToTheCart() {
        productSteps().selectThreeRandomProductsAndAddToCart(); // Agregar 3 productos aleatorios al carrito
    }

    @Then("el subtotal del carrito deberia ser igual a la suma de los precios de los {int} productos")
    public void theCartSubtotalShouldEqualTheSumOfProductPrices(int count) {
        productSteps().verifyCartTotal(); // Verificar que el subtotal del carrito sea igual a la suma de los precios de los productos seleccionados
    }
}
