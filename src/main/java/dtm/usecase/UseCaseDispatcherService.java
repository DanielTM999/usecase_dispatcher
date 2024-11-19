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
import java.util.function.Function;
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
        String pid = generatePID(null);
        return dispatcher(pid, clazz);
    }

    @Override
    public String dispatcher(Class<? extends UseCaseBase> clazz, Object... args) {
        String pid = generatePID(null);
        return dispatcher(pid, clazz, args);
    }
    
    @Override
    public String dispatcher(String PID, Class<? extends UseCaseBase> clazz) {
        String pid = generatePID(PID);
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
    public String dispatcher(String PID, Class<? extends UseCaseBase> clazz, Object... args) {
        String pid = generatePID(PID);
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
            validateARGS(initMethod, args);
            try{
                initMethod.setAccessible(true);
                return runMethodObject(entityObject, initMethod, args);
            }catch(Exception e){
                throw new UseCaseException(e.getCause());
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

    private String generatePID(String baseSeed){
        if(baseSeed == null || baseSeed.isEmpty()){
            baseSeed = UUID.randomUUID().toString();
        }

        return baseSeed;
    }

    private void validateARGS(final Method method, Object[] args){
        if(args == null){
            args = new Object[0];
        }
        if(method.getParameterCount() != args.length){
            throw new RuntimeException("A quantidade argumantos fornecidos não correspondem expected: "+method.getParameterCount() + " find: "+args.length);
        }
        
    }

    private class UseCaseResultData extends UseCaseResult{
        final private String pid;
        final private CompletableFuture<Object> action;
        private Function<Throwable, ? extends Exception> exceptionHandler;

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
            } catch (final InterruptedException | ExecutionException | RuntimeException e) {
                final Throwable root = e.getCause();
                if (exceptionHandler != null) {
                    throw exceptionHandler.apply(root);
                } else {
                    return null;
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T, E extends Exception> T get(Class<E> extBase) throws E{
            T result;
            try {
                result = (T) action.get();
                return result;
            } catch (final InterruptedException | ExecutionException | RuntimeException e) {
                final Throwable root = e.getCause();
                if (exceptionHandler != null) {
                    throw (E) exceptionHandler.apply(root));
                } else {
                    return null;
                }
            }
        }

        @Override
        public UseCaseResult ifThrow(Function<Throwable, ? extends Exception> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        @Override
        public String getPID() {
          return this.pid;
        }

    }

}
