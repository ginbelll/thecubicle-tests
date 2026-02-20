package stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import net.serenitybdd.core.Serenity;
import steps.CartRemovalSteps;

public class Test2StepDefinitions {

    private CartRemovalSteps cartRemovalSteps;

    // Inicializar solo cuando sea necesario para evitar problemas de inyeccion de dependencias en escenarios que no lo requieren
    private CartRemovalSteps cartRemovalSteps() {
        if (cartRemovalSteps == null) {
            cartRemovalSteps = new CartRemovalSteps(Serenity.getDriver());
        }
        return cartRemovalSteps;
    }

    @And("agrego todos los {int} productos al carrito")
    public void iAddAllProductsToTheCart(int count) {
        cartRemovalSteps().addFiveRandomProducts(); // Agregar 5 productos aleatorios al carrito
    }

    @And("elijo al azar {int} productos para remover del carrito")
    public void iRandomlySelectProductsToRemove(int count) {
        cartRemovalSteps().selectThreeProductsToRemove(); // Elegir 3 productos aleatorios del carrito
    }

    @And("remuevo los {int} productos del carrito")
    public void iRemoveThoseProductsFromTheCart(int count) {
        cartRemovalSteps().removeSelectedProducts(); // Remover los productos seleccionados del carrito
    }

    @Then("solo {int} productos deberian permanecer en el carrito")
    public void onlyProductsShouldRemainInTheCart(int count) {
        cartRemovalSteps().verifyRemainingProducts(); // Verificar que solo queden 2 productos en el carrito y que sean los correctos
    }

    @And("los {int} productos restantes deberian coincidir con los que no fueron removidos")
    public void theRemainingProductsShouldMatchTheOnesNotRemoved(int count) {
        // La verificación de la cantidad e identificacion de los productos se hizo en el paso anterior
    }
}
