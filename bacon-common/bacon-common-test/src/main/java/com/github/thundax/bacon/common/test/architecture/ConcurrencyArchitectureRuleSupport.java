package com.github.thundax.bacon.common.test.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

public final class ConcurrencyArchitectureRuleSupport {

    private ConcurrencyArchitectureRuleSupport() {}

    public static JavaClasses importProjectClasses(String basePackage) {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(basePackage);
    }

    public static ArchRule shouldNotCreateRawThreads(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + "..")
                .should(callMethod("java.lang.Thread", "<init>", "禁止 new Thread，统一使用受控线程池"))
                .because("Use managed executors. Do not create raw threads.");
    }

    public static ArchRule shouldNotUseExecutorsFactory(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + "..")
                .should(callOwner("java.util.concurrent.Executors", "禁止 Executors.new*，统一使用受控线程池"))
                .because("Use managed executors with TaskDecorator. Do not use Executors factories.");
    }

    public static ArchRule shouldNotUseCompletableFutureAsyncWithoutExecutor(String basePackage) {
        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(basePackage + "..")
                .should(new ArchCondition<>("call CompletableFuture async factory without explicit Executor") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        for (JavaMethodCall methodCall : item.getMethodCallsFromSelf()) {
                            if (!"java.util.concurrent.CompletableFuture"
                                    .equals(methodCall.getTargetOwner().getFullName())) {
                                continue;
                            }
                            String methodName = methodCall.getName();
                            int parameterCount =
                                    methodCall.getTarget().getRawParameterTypes().size();
                            boolean illegal = ("runAsync".equals(methodName) || "supplyAsync".equals(methodName))
                                    && parameterCount == 1;
                            if (illegal) {
                                events.add(SimpleConditionEvent.violated(item, methodCall.getDescription()));
                            }
                        }
                    }
                })
                .because("Async work must use an explicit managed executor.");
    }

    private static ArchCondition<JavaClass> callMethod(String owner, String methodName, String description) {
        return new ArchCondition<>(description) {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                for (JavaMethodCall methodCall : item.getMethodCallsFromSelf()) {
                    if (owner.equals(methodCall.getTargetOwner().getFullName())
                            && methodName.equals(methodCall.getName())) {
                        events.add(SimpleConditionEvent.violated(item, methodCall.getDescription()));
                    }
                }
            }
        };
    }

    private static ArchCondition<JavaClass> callOwner(String owner, String description) {
        return new ArchCondition<>(description) {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                for (JavaMethodCall methodCall : item.getMethodCallsFromSelf()) {
                    if (owner.equals(methodCall.getTargetOwner().getFullName())) {
                        events.add(SimpleConditionEvent.violated(item, methodCall.getDescription()));
                    }
                }
            }
        };
    }
}
