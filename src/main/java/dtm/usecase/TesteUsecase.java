package dtm.usecase;

import dtm.usecase.annotatins.InitCase;
import dtm.usecase.annotatins.UseCase;
import dtm.usecase.core.UseCaseBase;
import dtm.usecase.enums.Retention;

@UseCase(id="teste", scope=Retention.THIS)
public class TesteUsecase extends UseCaseBase{

    @InitCase
    public int init(int x) throws Exception{
        System.out.println("executando caso de uso");
        if(x > 0){
            return x;
        }else{
            throw new Exception();
        }
    }

}
