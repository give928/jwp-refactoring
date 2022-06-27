package kitchenpos.dependency;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "kitchenpos", importOptions = {ImportOption.DoNotIncludeTests.class})
@DisplayName("레이어드 아키텍처 의존성 테스트")
class LayeredArchitectureRuleTest {
    /**
     * ui 레이어는 아무 레이어에서도 접근할 수 없다.
     * application 레이어는 ui, application 레이어에서만 접근할 수 있다.
     * domain 레이어는 application, dto 레이어에서만 접근할 수 있다.
     * dto 레이어는 ui, application 레이어에서만 접근할 수 있다.
     *
     * ui -> application -> domain
     */
    @ArchTest
    ArchRule 레이어드_아키텍처_규칙 = layeredArchitecture()
            .layer("ui").definedBy("..ui..")
            .layer("application").definedBy("..application..")
            .layer("domain").definedBy("..domain..")
            .layer("dto").definedBy("..dto..")
            .whereLayer("ui").mayNotBeAccessedByAnyLayer()
            .whereLayer("application").mayOnlyBeAccessedByLayers("ui", "application")
            .whereLayer("domain").mayOnlyBeAccessedByLayers("application", "dto")
            .whereLayer("dto").mayOnlyBeAccessedByLayers("ui", "application");
}
