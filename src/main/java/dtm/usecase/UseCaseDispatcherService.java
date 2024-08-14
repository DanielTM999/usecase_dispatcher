package dtm.usecase;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import dtm.usecase.annotatins.UseCase;
import dtm.usecase.core.UseCaseBase;
import dtm.usecase.core.UseCaseDispatcher;
import dtm.usecase.core.UseCaseResult;

public class UseCaseDispatcherService implements UseCaseDispatcher{
    private static Map<String, CompletableFuture<Object>> useCases;
    private Map<String, CompletableFuture<Object>> useCasesScoped;

    @Override
    public void dispatcher(Class<? extends UseCaseBase> clazz) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dispatcher'");
    }

    @Override
    public void dispatcher(Class<? extends UseCaseBase> clazz, Object... args) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dispatcher'");
    }
    
    @Override
    public UseCaseResult getUseCase(String caseId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUseCase'");
    }

    private Object initializeUseCaseObject(Class<?> clazz){
        Object entityObject = null;
        return entityObject;
    }
    
    private String getPinId(Class<? extends UseCaseBase> clazz){
        if(clazz.isAnnotationPresent(UseCase.class)){
            UseCase useCase = clazz.getAnnotation(UseCase.class);
            String pin = useCase.id();
            if(pin == null || pin.isEmpty()){
                return UUID.randomUUID().toString();
            }
            return pin;
        }else{
            throw new RuntimeException("classe de caso de uso sem anotação presente: ["+clazz.getName()+"]");
        }
    }
}
