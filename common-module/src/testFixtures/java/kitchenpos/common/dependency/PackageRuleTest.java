package kitchenpos.common.dependency;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.DisplayName;

/**
 * Deprecated
 * 3단계까지 사용
 */
@AnalyzeClasses(packages = "kitchenpos", importOptions = {ImportOption.DoNotIncludeTests.class})
@DisplayName("패키지 의존성 테스트")
class PackageRuleTest {
    public static final String MENU = "..menu..";
    public static final String PRODUCT = "..product..";
    public static final String ORDER = "..order..";
    public static final String TABLE = "..table..";
    public static final String KITCHENPOS_PACKAGE = "..kitchenpos.(*)..";
    /**
     * 상품 <- 메뉴
     */
    @ArchTest
    ArchRule 상품_패키지_규칙 = ArchRuleDefinition.classes().that().resideInAPackage(PRODUCT)
            .should().onlyHaveDependentClassesThat().resideInAnyPackage(PRODUCT, MENU);

    /**
     * 메뉴 <- 주문
     */
    @ArchTest
    ArchRule 메뉴_패키지_규칙 = ArchRuleDefinition.classes().that().resideInAPackage(MENU)
            .should().onlyHaveDependentClassesThat().resideInAnyPackage(MENU, ORDER);

    /**
     * 주문
     */
    @ArchTest
    ArchRule 주문_패키지_규칙 = ArchRuleDefinition.classes().that().resideInAPackage(ORDER)
            .should().onlyHaveDependentClassesThat().resideInAnyPackage(ORDER);

    /**
     * 테이블 <- 주문
     */
    @ArchTest
    ArchRule 주문_테이블_패키지_규칙 = ArchRuleDefinition.classes().that().resideInAPackage(TABLE)
            .should().onlyHaveDependentClassesThat().resideInAnyPackage(TABLE, ORDER);

    /**
     * 패키지 간 순환 참조가 없어야 한다.
     */
    @ArchTest
    ArchRule 패키지_순환참조 = SlicesRuleDefinition.slices().matching(KITCHENPOS_PACKAGE)
            .should().beFreeOfCycles();
}
