package dtm.usecase.core;


import java.util.function.Function;


public abstract class UseCaseResult {
   public abstract boolean isDone();
   public abstract String getPID();
   public abstract <T, E extends Exception> T await() throws E;
   public abstract <T, E extends Exception> T await(Function<Exception, E> exceptionHandler) throws E;
   public abstract <T, E extends Exception> T await(Class<E> extBase) throws E;
   public abstract UseCaseResult ifThrow(Function<Exception, ? extends Exception> exceptionHandler);
}
