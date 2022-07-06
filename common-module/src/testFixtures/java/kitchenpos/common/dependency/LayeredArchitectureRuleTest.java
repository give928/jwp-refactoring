package kitchenpos.common.dependency;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.DisplayName;

/**
 * Deprecated
 * 3단계까지 사용
 */
@AnalyzeClasses(packages = "kitchenpos", importOptions = {ImportOption.DoNotIncludeTests.class})
@DisplayName("레이어드 아키텍처 의존성 테스트")
class LayeredArchitectureRuleTest {
    public static final String DOMAIN_LAYER = "domain";
    public static final String DOMAIN_PACKAGE = "..domain..";
    public static final String APPLICATION_LAYER = "application";
    public static final String APPLICATION_PACKAGE = "..application..";
    public static final String UI_LAYER = "ui";
    public static final String UI_PACKAGE = "..ui..";
    public static final String DTO_LAYER = "dto";
    public static final String DTO_PACKAGE = "..dto..";

    /**
     * ui 레이어는 아무 레이어에서도 접근할 수 없다.
     * application 레이어는 ui, application 레이어에서만 접근할 수 있다.
     * domain 레이어는 application, dto 레이어에서만 접근할 수 있다.
     * dto 레이어는 ui, application 레이어에서만 접근할 수 있다.
     *
     * ui -> application -> domain
     */
    @ArchTest
    ArchRule 레이어드_아키텍처_규칙 = Architectures.layeredArchitecture()
            .layer(UI_LAYER).definedBy(UI_PACKAGE)
            .layer(APPLICATION_LAYER).definedBy(APPLICATION_PACKAGE)
            .layer(DOMAIN_LAYER).definedBy(DOMAIN_PACKAGE)
            .layer(DTO_LAYER).definedBy(DTO_PACKAGE)
            .whereLayer(UI_LAYER).mayNotBeAccessedByAnyLayer()
            .whereLayer(APPLICATION_LAYER).mayOnlyBeAccessedByLayers(UI_LAYER, APPLICATION_LAYER)
            .whereLayer(DOMAIN_LAYER).mayOnlyBeAccessedByLayers(APPLICATION_LAYER, DTO_LAYER)
            .whereLayer(DTO_LAYER).mayOnlyBeAccessedByLayers(UI_LAYER, APPLICATION_LAYER);
}
