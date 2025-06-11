package dtm.usecase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import dtm.usecase.annotatins.InitCase;
import dtm.usecase.annotatins.UseCase;
import dtm.usecase.core.PidUseCaseResult;
import dtm.usecase.core.UseCaseBase;
import dtm.usecase.core.UseCaseDispatcher;
import dtm.usecase.core.UseCaseException;
import dtm.usecase.core.UseCaseResult;
import dtm.usecase.core.exceptions.InitializeUseCaseException;
import dtm.usecase.enums.Retention;
import dtm.usecase.results.UseCaseResultData;

public class UseCaseDispatcherService implements UseCaseDispatcher{
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final Map<String, CompletableFuture<Object>> useCasesAplication = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<Object>> useCasesScoped;

    public UseCaseDispatcherService(){
        useCasesScoped = new ConcurrentHashMap<>();
    }

    @SafeVarargs
    @Override
    public final List<PidUseCaseResult> dispatchList(Class<? extends UseCaseBase>... clazzList) {
        List<PidUseCaseResult> pidList = new ArrayList<>();
        for(Class<? extends UseCaseBase> useCaseBase : clazzList){
            String pid = dispatcher(useCaseBase);
            pidList.add(new PidUseCaseResult(pid, useCaseBase));
        }
        return pidList;
    }

    @Override
    public List<PidUseCaseResult> dispatchList(List<Class<? extends UseCaseBase>> clazzList, Object... args) {
        List<PidUseCaseResult> pidList = new ArrayList<>();
        for(Class<? extends UseCaseBase> useCaseBase : clazzList){
            String pid = dispatcher(useCaseBase, args);
            pidList.add(new PidUseCaseResult(pid, useCaseBase));
        }
        return pidList;
    }

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
        injectToQueue(clazz, pid, getQueueByScope(retention), null);
        return pid;
    }

    @Override
    public String dispatcher(String PID, Class<? extends UseCaseBase> clazz, Object... args) {
        String pid = generatePID(PID);
        Retention retention = getScope(clazz);
        injectToQueue(clazz, pid, getQueueByScope(retention), args);
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
            if (clazz.getConstructors().length > 0 && clazz.getConstructors()[0].getParameterCount() > 0) {
                throw new InitializeUseCaseException("A classe do caso de uso deve possuir apenas construtor vazio.");
            }
            entityObject = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new InitializeUseCaseException(e);
        }
        return entityObject;
    }

    private Method getInitialMethod(Class<? extends UseCaseBase> clazz){
        List<Method> methods = Arrays.asList(clazz.getDeclaredMethods());
        List<Method> methodsFilters = methods.stream().parallel()
            .filter(e -> (e.isAnnotationPresent(InitCase.class)))
            .toList();

        if(methodsFilters.isEmpty()){
            throw new InitializeUseCaseException("classe de caso de uso sem anotação de inicialização (@InitCase): ["+clazz.getName()+"]");
        }

        if (methodsFilters.size() > 1) {
            throw new InitializeUseCaseException("Mais de um método com @InitCase encontrado na classe [" + clazz.getName() + "]");
        }

        return methodsFilters.getFirst();
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
            throw new InitializeUseCaseException("classe de caso de uso sem anotação presente: ["+clazz.getName()+"]");
        }
    }

    private void injectToQueue(Class<? extends UseCaseBase> clazz, String pid, Map<String, CompletableFuture<Object>> useCases, final Object[] args){
        Object entityObject = initializeUseCaseObject(clazz);
        Method initMethod = getInitialMethod(clazz);
        CompletableFuture<Object> result = CompletableFuture.supplyAsync(() -> {
            validateARGS(initMethod, args);
            try{
                initMethod.setAccessible(true);
                return runMethodObject(entityObject, initMethod, args);
            }catch(Exception e){
                Throwable rootCause = getRootCause(e);
                throw new UseCaseException(rootCause instanceof Exception ? (Exception) rootCause : new Exception("Erro desconhecido", rootCause));
            }
        }, executorService);

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

    private String generatePID(String baseSeed){
        if(baseSeed == null || baseSeed.isEmpty()){
            baseSeed = UUID.randomUUID().toString();
        }

        return baseSeed;
    }

    private void validateARGS(final Method method, Object[] args) {
        if (args == null) {
            args = new Object[0];
        }

        int expectedCount = method.getParameterCount();
        int providedCount = args.length;

        if (expectedCount != providedCount) {
            String expectedTypes = Arrays.toString(Arrays.stream(method.getParameterTypes())
                    .map(Class::getSimpleName)
                    .toArray());

            String providedTypes = Arrays.toString(Arrays.stream(args)
                    .map(arg -> arg == null ? "null" : arg.getClass().getSimpleName())
                    .toArray());

            throw new IllegalArgumentException(
                    String.format("Quantidade de argumentos incompatível. Esperado: %d %s, fornecido: %d %s",
                            expectedCount, expectedTypes, providedCount, providedTypes)
            );
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        int limit = 15;
        while (cause.getCause() != null && limit-- > 0) {
            cause = cause.getCause();
        }
        return cause;
    }

    private Map<String, CompletableFuture<Object>> getQueueByScope(Retention retention) {
        return retention == Retention.ANY ? useCasesAplication : useCasesScoped;
    }


}
