package kitchenpos.dependency;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "kitchenpos", importOptions = {ImportOption.DoNotIncludeTests.class})
@DisplayName("패키지 의존성 테스트")
class PackageRuleTest {
    /**
     * 상품 <- 메뉴
     */
    @ArchTest
    ArchRule 상품_패키지_규칙 = classes().that().resideInAPackage("..product..")
            .should().onlyHaveDependentClassesThat().resideInAnyPackage("..product..", "..menu..");

    /**
     * 메뉴 <- 주문
     */
    @ArchTest
    ArchRule 메뉴_패키지_규칙 = classes().that().resideInAPackage("..menu..")
            .should().onlyHaveDependentClassesThat().resideInAnyPackage("..menu..", "..order..");

    /**
     * 주문
     */
    @ArchTest
    ArchRule 주문_패키지_규칙 = classes().that().resideInAPackage("..order..")
            .should().onlyHaveDependentClassesThat().resideInAnyPackage("..order..");

    /**
     * 테이블 <- 주문
     */
    @ArchTest
    ArchRule 주문_테이블_패키지_규칙 = classes().that().resideInAPackage("..table..")
            .should().onlyHaveDependentClassesThat().resideInAnyPackage("..table..", "..order..");

    /**
     * 패키지 간 순환 참조가 없어야 한다.
     */
    @ArchTest
    ArchRule 패키지_순환참조 = slices().matching("..kitchenpos.(*)..")
            .should().beFreeOfCycles();
}
