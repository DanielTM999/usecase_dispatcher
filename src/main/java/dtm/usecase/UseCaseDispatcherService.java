package dtm.usecase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import dtm.usecase.annotatins.InitCase;
import dtm.usecase.annotatins.UseCase;
import dtm.usecase.core.UseCaseBase;
import dtm.usecase.core.UseCaseDispatcher;
import dtm.usecase.core.UseCaseException;
import dtm.usecase.core.UseCaseResult;
import dtm.usecase.enums.Retention;

public class UseCaseDispatcherService implements UseCaseDispatcher{
    private static Map<String, CompletableFuture<Object>> useCasesAplication;
    private Map<String, CompletableFuture<Object>> useCasesScoped;

    @Override
    public String dispatcher(Class<? extends UseCaseBase> clazz) {
        String pid = getPinId(clazz);
        Retention retention = getScope(clazz);

        if(retention == Retention.ANY){
            startMap();
            injectToQueue(clazz, pid, useCasesAplication, null);
        }else{
            startMap();
            injectToQueue(clazz, pid, useCasesScoped, null);
        }
        return pid;
    }

    @Override
    public String dispatcher(Class<? extends UseCaseBase> clazz, Object... args) {
        String pid = getPinId(clazz);
        Retention retention = getScope(clazz);

        if(retention == Retention.ANY){
            startMap();
            injectToQueue(clazz, pid, useCasesScoped, args);
        }else{
            startMap();
            injectToQueue(clazz, pid, useCasesAplication, args);
        }
        return pid;
    }
    
    @Override
    public UseCaseResult getUseCase(String caseId) {
        if(useCasesAplication.containsKey(caseId)){
            return new UseCaseResultData(caseId, useCasesAplication.get(caseId));
        }else if(useCasesScoped.containsKey(caseId)){
            return new UseCaseResultData(caseId, useCasesScoped.get(caseId));
        }
        throw new RuntimeException("useCase com pin: '"+caseId+"' não encontrado");
    }

    private Object initializeUseCaseObject(Class<?> clazz){
        Object entityObject = null;
        try {
            entityObject = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return entityObject;
    }

    private Method getInitialMethod(Class<? extends UseCaseBase> clazz){
        List<Method> methods = Arrays.asList(clazz.getDeclaredMethods());
        List<Method> methodsFilters = methods.stream().parallel()
            .filter(e -> (e.isAnnotationPresent(InitCase.class)))
            .collect(Collectors.toList());

        if(methodsFilters.isEmpty()){
            throw new RuntimeException("classe de caso de uso sem anotação de inicialização (@InitCase): ["+clazz.getName()+"]");
        }

        return methodsFilters.get(0);
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

    private Retention getScope(Class<? extends UseCaseBase> clazz){
        if(clazz.isAnnotationPresent(UseCase.class)){
            UseCase useCase = clazz.getAnnotation(UseCase.class);
            Retention retention = useCase.scope();
            if(retention == null){
                retention = Retention.THIS;
            }
            return retention;
        }else{
            throw new RuntimeException("classe de caso de uso sem anotação presente: ["+clazz.getName()+"]");
        }
    }

    private void injectToQueue(Class<? extends UseCaseBase> clazz, String pid, Map<String, CompletableFuture<Object>> useCases, final Object[] args){
        CompletableFuture<Object> result = CompletableFuture.supplyAsync(() -> {
            Object entityObject = initializeUseCaseObject(clazz);
            Method initMethod = getInitialMethod(clazz);
            try{
                initMethod.setAccessible(true);
                return runMethodObject(entityObject, initMethod, args);
            }catch(Exception e){
                throw new UseCaseException("Interrompendo caso de uso:"+clazz.getName()+" por falha");
            }
        });
        useCases.put(pid, result);
    }

    private Object runMethodObject(Object targetEntity, Method method, final Object[] args) throws Exception{
        if(method.canAccess(targetEntity)){
            method.setAccessible(true);
        }
        Object objResult;
        if(method.getParameterCount() > 0){
            objResult = method.invoke(targetEntity, args);
        }else{
            objResult = method.invoke(targetEntity);
        }
        
        return objResult;
    }

    private void startMap(){
        if(useCasesAplication == null){
            useCasesAplication = new HashMap<>();
        }

        if(useCasesScoped == null){
            useCasesScoped = new HashMap<>();
        }
    }

    private class UseCaseResultData extends UseCaseResult{
        final private String pid;
        final private CompletableFuture<Object> action;
        private Supplier<? extends Exception> exception;

        public UseCaseResultData(String pid, CompletableFuture<Object> action){
            this.pid = pid;
            this.action = action; 
        }

        @Override
        public boolean isDone() {
            return action.isDone();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T get() throws Exception{
            T result;
            try {
                result = (T) action.get();
                return result;
            } catch (final InterruptedException | ExecutionException e) {
                System.out.println(e.getMessage());
                if(exception == null){
                    throw e;
                }else{
                    throw exception.get();
                }
            }
        }

        @Override
        public UseCaseResult ifThrow(Supplier<? extends Exception> exception) {
            this.exception = exception;
            return this;
        }

        @Override
        public String getPID() {
          return this.pid;
        }

    }

}
