package dtm.usecase.core;

import java.util.List;

public interface UseCaseDispatcher {
    List<PidUseCaseResult> dispatchList(List<Class<? extends UseCaseBase>> clazzList);
    String dispatcher(Class<? extends UseCaseBase> clazz);
    String dispatcher(Class<? extends UseCaseBase> clazz, Object... args);
    String dispatcher(String PID, Class<? extends UseCaseBase> clazz);
    String dispatcher(String PID, Class<? extends UseCaseBase> clazz, Object... args);
    UseCaseResult getUseCase(String caseId);
}
