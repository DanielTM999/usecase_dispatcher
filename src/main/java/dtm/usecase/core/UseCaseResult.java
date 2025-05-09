package dtm.usecase.core;


import java.util.function.Function;


public abstract class UseCaseResult {
   public abstract boolean isDone();
   public abstract String getPID();
   public abstract <T> T await() throws Exception;
   public abstract <T> T await(Function<Exception, ? extends Exception> exceptionHandler) throws Exception;
   public abstract <T, E extends Exception> T await(Class<E> extBase) throws E;
   public abstract UseCaseResult ifThrow(Function<Exception, ? extends Exception> exceptionHandler);
}
