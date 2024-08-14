package dtm.usecase;


import dtm.usecase.core.UseCaseBase;
import dtm.usecase.core.UseCaseDispatcher;


public class Main extends UseCaseBase{

    public static void main(String[] args) throws Exception {
        UseCaseDispatcher useCaseDispatcher = new UseCaseDispatcherService();
        String pid = useCaseDispatcher.dispatcher(TesteUsecase.class, 2);
        useCaseDispatcher.getUseCase(pid)
            .ifThrow(() -> new Exception("teste"))
        .get();
    }

}