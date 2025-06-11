# UseCaseDispatcherService

O `UseCaseDispatcherService` é uma implementação da interface `UseCaseDispatcher` que gerencia a execução de casos de uso (use cases) de forma assíncrona utilizando `CompletableFuture`. Ele permite a execução de casos de uso em diferentes escopos, como aplicação e sessão, e possibilita a obtenção dos resultados de forma controlada.

## Índice

- [Visão Geral](#visão-geral)
- [instalação](#instalação)
- [Funcionalidades](#funcionalidades)
- [Estrutura do Código](#estrutura-do-código)
- [Exemplo de Uso](#exemplo-de-uso)
- [Anotações](#anotações)
- [Enumeração Retention](#enumeração-retention)
- [Erros e Exceções](#erros-e-exceções)

## Visão Geral

O `UseCaseDispatcherService` gerencia a execução de casos de uso identificados por um PID (Process ID) único. Ele permite a execução de métodos anotados com `@InitCase` dentro de uma classe que representa um caso de uso (`UseCaseBase`).

A classe suporta dois escopos principais:

- **ANY**: Escopo de aplicação, compartilhado por toda a aplicação.
- **THIS**: Escopo específico, isolado por sessão ou instância.

## instalação

## Instalação

### Instalação com Maven

**Adicionar o Repositório**

Adicione o repositório público ao seu arquivo `pom.xml`:

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/DanielTM999/usecase_dispatcher</url>
  </repository>
</repositories>
```

**Adicionar a Dependência**

Em seguida, adicione a dependência do UseCaseDispatcherService ao seu pom.xml. Verifique a versão mais recente no repositório GitHub e ajuste a versão conforme necessário:

```xml
<dependencies>
  <dependency>
    <groupId>com.example</groupId>
    <artifactId>usecase-dispatcher</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```
Certifique-se de substituir com.example e usecase-dispatcher pelos valores corretos conforme definido no repositório.


## Funcionalidades

- **Execução Assíncrona**: Utiliza `CompletableFuture` para execução não bloqueante dos casos de uso.
- **Suporte a Múltiplos Escopos**: Casos de uso podem ser executados no escopo da aplicação ou de forma isolada.
- **Manuseio de Exceções**: Permite a definição de handlers de exceção personalizados para tratar falhas durante a execução dos casos de uso.
- **Geração de PID**: Cada caso de uso é identificado por um PID único, facilitando o rastreamento e recuperação do resultado da execução.

## Estrutura do Código

### Interface `UseCaseDispatcher`

Define os métodos que devem ser implementados pelo `UseCaseDispatcherService`:

- `dispatcher(Class<? extends UseCaseBase> clazz)`: Executa o caso de uso especificado.
- `dispatcher(Class<? extends UseCaseBase> clazz, Object... args)`: Executa o caso de uso com argumentos adicionais.
- `dispatcher(String PID, Class<? extends UseCaseBase> clazz)`: Executa o caso de uso especificado com um PID personalizado.
- `dispatcher(String PID, Class<? extends UseCaseBase> clazz, Object... args)`: Executa o caso de uso com um PID personalizado e argumentos adicionais.
- `getUseCase(String caseId)`: Retorna o resultado de um caso de uso baseado no seu PID.

### Classe `UseCaseDispatcherService`

Implementa a interface `UseCaseDispatcher` e fornece a lógica de execução dos casos de uso.

- **`dispatcher`**: Métodos que iniciam a execução de casos de uso com ou sem argumentos adicionais.
- **`getUseCase`**: Método para recuperar o resultado de um caso de uso utilizando seu PID.
- **`initializeUseCaseObject`**: Inicializa a instância de um caso de uso.
- **`getInitialMethod`**: Retorna o método inicial anotado com `@InitCase`.
- **`injectToQueue`**: Injeta o caso de uso em uma fila para execução assíncrona.
- **`runMethodObject`**: Executa o método inicial do caso de uso.
- **`generatePID`**: Gera um PID único para identificar o caso de uso.
- **`validateARGS`**: Valida os argumentos fornecidos para o método inicial do caso de uso.

### Classe `UseCaseResultData`

Classe interna que estende `UseCaseResult` e encapsula o resultado da execução do caso de uso. 

- **`isDone`**: Verifica se a execução do caso de uso foi concluída.
- **`get`**: Retorna o resultado da execução, com suporte a exceções personalizadas.
- **`ifThrow`**: Define um handler de exceções personalizado.
- **`getPID`**: Retorna o PID associado ao caso de uso.

### Anotações

#### `@UseCase`

Define a anotação `@UseCase` para marcar classes como casos de uso.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UseCase {
    dtm.usecase.enums.Retention scope();
}
```
#### `@InitCase`

Define a anotação `@InitCase` para marcar o método inicial de um caso de uso.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InitCase {
}
```

### Enumeração Retention

Define os escopos de retenção dos casos de uso.

```java
public enum Retention {
    ANY,
    THIS;
}
```

### Exemplo de Uso

```java
@UseCase(scope = Retention.ANY)
public class MeuCasoDeUso extends UseCaseBase {

    @InitCase
    public void iniciar() {
        // Lógica do caso de uso
    }
}

// Dispatcher
UseCaseDispatcher dispatcher = new UseCaseDispatcherService();
String pid = dispatcher.dispatcher(MeuCasoDeUso.class);

// Obter resultado com tratamento de exceção
try {
    dispatcher.getUseCase(pid)
            .ifThrow((exceptionBase) -> new UseCaseException("Erro ao executar o caso de uso: " + exceptionBase.getMessage()))
            .awat(UseCaseException.class);
} catch (UseCaseException e) {
    // Tratamento da exceção
    e.printStackTrace();
}
```

## Erros e Exceções

- **RuntimeException**: Lançada em casos como ausência de anotações obrigatórias ou falhas de execução.
- **UseCaseException**: Lançada durante a execução de um caso de uso, caso uma falha ocorra.

## Contribuindo

Se você deseja contribuir com melhorias para este projeto, sinta-se à vontade para abrir um pull request ou uma issue.

## Licença

Este projeto é licenciado sob a licença MIT.
