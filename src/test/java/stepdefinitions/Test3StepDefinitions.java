package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import net.serenitybdd.core.Serenity;
import steps.BrandFilterSteps;

public class Test3StepDefinitions {

    private BrandFilterSteps brandFilterSteps;

    // Inicializar solo cuando sea necesario para evitar problemas de inyeccion de dependencias en escenarios que no lo requieren
    private BrandFilterSteps brandFilterSteps() {
        if (brandFilterSteps == null) {
            brandFilterSteps = new BrandFilterSteps(Serenity.getDriver());
        }
        return brandFilterSteps;
    }

    @Given("que estoy en la pagina de marcas populares de TheCubicle")
    public void iAmOnThePopularBrandsPage() {
        brandFilterSteps().openPopularBrandsPage(); // Abrir pagina de marcas populares de TheCubicle
    }

    @When("selecciono una marca aleatoria de la pagina")
    public void iSelectARandomBrandFromThePage() {
        brandFilterSteps().selectRandomBrand(); // Seleccionar una marca aleatoria y navegar a su pagina de productos
    }

    @And("navego a la pagina de productos de esa marca")
    public void iNavigateToThatBrandsProductPage() {
        // La navegación se realiza en el paso anterior para mantener la coherencia del flujo de usuario
    }

    @And("ordeno los productos alfabeticamente de la A a la Z")
    public void iSortTheProductsAlphabeticallyFromAToZ() {
        brandFilterSteps().sortProductsAlphabetically(); // Ordenar los productos de la marca seleccionada alfabeticamente de la A a la Z
    }

    @Then("todos los productos en la primera pagina deberian estar en orden alfabetico")
    public void allProductsOnTheFirstPageShouldBeInAlphabeticalOrder() {
        brandFilterSteps().verifyAlphabeticalOrder(); // Verificar que los productos en la primera pagina esten ordenados alfabeticamente de la A a la Z
    }
}
