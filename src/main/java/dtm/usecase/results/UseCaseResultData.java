package dtm.usecase.results;

import dtm.usecase.core.UseCaseResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class UseCaseResultData extends UseCaseResult {
    final private String pid;
    final private CompletableFuture<Object> action;
    private Function<Exception, ? extends Exception> exceptionHandler;

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
    public <T, E extends Exception> T await() throws E{
        T result;
        try {
            result = (T) action.get();
            return result;
        } catch (final InterruptedException | ExecutionException | RuntimeException e) {
            final Throwable root = e.getCause();
            if (exceptionHandler != null) {
                final Exception error = getRootCauseAsException(root.getCause() == null ? root : root.getCause());
                throw (E)exceptionHandler.apply(error);
            } else {
                return null;
            }
        }
    }

    @Override
    public <T, E extends Exception> T await(Function<Exception, E> exceptionHandler) throws E {
        this.exceptionHandler = exceptionHandler;
        return await();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, E extends Exception> T await(Class<E> extBase) throws E{
        T result;
        try {
            result = (T) action.get();
            return result;
        } catch (final InterruptedException | ExecutionException | RuntimeException e) {
            final Throwable root = e.getCause();
            if (exceptionHandler != null) {
                final Exception error = getRootCauseAsException(root.getCause() == null ? root : root.getCause());
                throw (E) exceptionHandler.apply(error);
            } else {
                return null;
            }
        }
    }

    @Override
    public UseCaseResult ifThrow(Function<Exception, ? extends Exception> exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    public String getPID() {
        return this.pid;
    }

    private Exception getRootCauseAsException(Throwable throwable) {
        Throwable cause = throwable;
        int cont = 0;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            cont++;
            if(cont == 15){
                break;
            }
        }
        return (cause == null) ? new Exception("Undefined cause") : (cause instanceof Exception) ? (Exception) cause : new Exception("Root cause is not an Exception", cause);
    }

}
