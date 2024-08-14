package dtm.usecase;

import dtm.usecase.annotatins.InitCase;
import dtm.usecase.annotatins.UseCase;
import dtm.usecase.core.UseCaseBase;
import dtm.usecase.core.UseCaseDispatcher;
import dtm.usecase.enums.Retention;

@UseCase(id = "caso1", scope = Retention.ANY)
public class Main extends UseCaseBase{

    public static void main(String[] args) throws Exception {
        UseCaseDispatcher useCaseDispatcher = new UseCaseDispatcherService();
        useCaseDispatcher.dispatcher(Main.class);

        int i = useCaseDispatcher.getUseCase("knvpw")
            .ifThrow(null)
            .get();
    }


    @InitCase
    private int initCase(){
        return 0;
    }

}