package dtm.usecase.core;


import java.util.function.Function;


public abstract class UseCaseResult {
   public abstract boolean isDone();
   public abstract String getPID();
   public abstract <T> T get() throws Exception;
   public abstract <T, E extends Exception> T get(Class<E> extBase) throws E;
   public abstract UseCaseResult ifThrow(Function<Throwable, ? extends Exception> exceptionHandler);
}
