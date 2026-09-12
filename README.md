# Checkpoint 4 — Bug Hunt StreamFIAP

## Identificação

**Grupo:** Individual

| Integrante | RM | Turma |
|---|---|---|
| Mateus Scandiuzzi Valente Tomomitsu | 561565 | 2CCPW |

| Campo | Resultado |
|---|---|
| **Total de bugs corrigidos** | 12 / 12 |
| **Total de ajustes de Clean Code** | 6 / 6 |

---

## Parte 1 — Bugs encontrados

| # | Sintoma observado (o que fiz/vi) | Causa raiz (arquivo e linha aproximada) | Correção aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | Ao buscar um ID inexistente, a API devolvia uma resposta vazia em vez da mensagem de conteúdo não encontrado. | `ConteudoController.java`, método `buscarPorId`: um `catch (Exception)` vazio engolia a exceção. | Removi o `try/catch` e deixei a `ConteudoNaoEncontradoException` chegar ao handler global. | Exceções e tratamento centralizado. |
| bug02 | A busca por `FICCAO` não encontrava conteúdos que tinham essa categoria. | `ConteudoController.java`, método `listarPorCategoria`: comparação de `String` com `==` e busca manual em todos os registros. | Passei a usar `conteudoRepository.findByCategoria(categoria)`. | Comparação de objetos e Spring Data JPA. |
| bug03 | Era possível cadastrar conteúdo com duração zero ou negativa. | `Conteudo.java`, construtor e setter: a duração era atribuída sem validação. | Validei `duracaoMinutos <= 0` no model e devolvi erro HTTP 400 com uma mensagem clara. | Encapsulamento, validação e exceções. |
| bug04 | A promoção de um filme aumentava seu preço em 20%. | `Filme.java`, método `aplicarPromocao`: o preço era multiplicado por `1.2`. | Corrigi o fator para aplicar 20% de desconto e arredondei o resultado para centavos. | Sobrescrita e regra de negócio. |
| bug05 | Um documentário custava R$ 9,90, embora devesse ser gratuito. | `Documentario.java`: a classe herdava o preço padrão e não sobrescrevia o cálculo. | Sobrescrevi `calcularPrecoAluguel()` para retornar R$ 0,00. | Herança e polimorfismo. |
| bug06 | Uma série era salva sem título, categoria, duração, classificação e disponibilidade. | `Serie.java`, construtor: não havia chamada ao construtor de `Conteudo` e nem parâmetro de disponibilidade. | Chamei `super(...)`, incluí `disponivel` e ajustei o controller. | Construtores e herança. |
| bug07 | Uma série com 5 temporadas custava R$ 9,90 em vez de R$ 24,50. | `Serie.java`: `calcularPrecoAluguel(double)` era uma sobrecarga, não a sobrescrita esperada. | Troquei a assinatura para `calcularPrecoAluguel()`, adicionei `@Override` e arredondei preços e promoções para centavos. | Sobrescrita versus sobrecarga. |
| bug08 | O cadastro de usuário não gerava ID e podia falhar ao salvar. | `Usuario.java`, campo `id`: existia `@Id`, mas faltava a estratégia de geração. | Adicionei `@GeneratedValue(strategy = GenerationType.IDENTITY)`. | JPA e persistência de entidades. |
| bug09 | O usuário era salvo com o nome nulo. | `Usuario.java`, construtor: `nome = nome` atribuía o parâmetro a ele mesmo. | Corrigi a atribuição para `this.nome = nome`. | Estado do objeto e uso de `this`. |
| bug10 | Um usuário com poucos créditos podia alugar e ficar negativo, enquanto um usuário com saldo alto podia ser recusado. | `Usuario.java`, método `temCreditosSuficientes`: a comparação estava invertida. | Passei a verificar `creditos >= preco` antes do débito e arredondei o saldo restante para centavos. | Regra de negócio e operadores relacionais. |
| bug11 | Um conteúdo marcado como indisponível ainda podia ser alugado. | `Usuario.java`, método `alugar`: não existia validação de disponibilidade. | Adicionei a validação e lancei `ConteudoIndisponivelException` antes de efetivar o aluguel. | Exceções de domínio e proteção de estado. |
| bug12 | A restrição de idade virava erro genérico e a mensagem da classificação não chegava ao cliente. | `GlobalExceptionHandler.java`: faltava um handler para `ClassificacaoIndicativaException`; ser checked não era o defeito. | Criei um handler que devolve HTTP 422 e a mensagem da regra. A mudança para `RuntimeException` foi uma escolha para seguir o padrão das outras exceções do projeto. | Exceções checked e unchecked e tratamento centralizado. |

## Parte 2 — Ajustes de Clean Code

| # | Onde estava | Qual princípio/boa prática era violado | O que eu mudei |
|---|---|---|---|
| clean01 | Campo `duracaoMinutos` de `Conteudo` e controllers. | Encapsulamento: o atributo era público e podia ser alterado sem passar pelo objeto. | Tornei o campo privado e passei a usar seu getter. |
| clean02 | Método `Usuario.alugar`. | Nomes como `c` e `p` não explicavam o conteúdo das variáveis. | Renomeei para `conteudo` e `preco`. |
| clean03 | Regras de preço em `Conteudo`, `Filme`, `Serie` e `Promocionavel`. | Números mágicos espalhados dificultavam entender e alterar as regras. | Criei constantes com nomes como `PRECO_BASE`, `ADICIONAL_ESTREIA` e `FATOR_DESCONTO`. |
| clean04 | Método `Usuario.alugar`. | Responsabilidade única: o model alterava o aluguel e também imprimia um recibo no console. | Removi os `System.out.println`, eliminando a impressão do recibo, que não é exigida pelo contrato da API. O método ficou responsável pela regra de aluguel. |
| clean05 | `ConteudoController`. | Código morto aumenta ruído e pode confundir futuras manutenções. | Removi o método privado `calcularDescontoAntigo`, que não era chamado. |
| clean06 | Final de `ConteudoController` e método `Usuario.debitarCreditos`. | Código comentado e comentários incorretos confundem a leitura. | Removi o bloco antigo de cupom e o comentário que dizia adicionar créditos, embora o método debite o valor. |

---

## Parte 3 — Perguntas de reflexão

### 1. Injeção de dependência (Aula 13)

O Spring encontra os repositories durante a inicialização e cria beans para eles.\
No caso de `ConteudoRepository`, ele cria um objeto proxy que implementa a interface.\
Esse proxy recebe o `EntityManager` e toda a configuração necessária para conversar com o banco.\
Quando o `@Autowired` é processado, o Spring coloca esse bean no campo do controller.\
Um `new ConteudoRepository()` nem seria possível, porque o repository é uma interface.\
Mesmo com uma classe concreta, o `new` comum não teria o gerenciamento de persistência e transações do Spring.

### 2. JDBC vs Spring Data JPA (Aulas 12 e 13)

No JDBC, nós abrimos a conexão, montamos o `PreparedStatement` e transformamos cada linha do `ResultSet`.\
O Spring Data JPA faz o CRUD, o mapeamento da entidade e boa parte do controle da conexão automaticamente.\
Por isso `ConteudoRepository` só precisa estender `JpaRepository` para ganhar `save`, `findAll` e `findById`.\
O método `findByCategoria` funciona porque o Spring interpreta seu nome e gera a consulta pela propriedade `categoria`.\
JDBC ainda pode ser melhor quando precisamos de uma consulta SQL muito específica ou controle fino de desempenho.\
Neste projeto, JPA reduz bastante o código repetido e é suficiente para as consultas simples da API.

### 3. Exceções checked vs unchecked (Aula 11)

Uma checked exception, que herda de `Exception`, obriga quem chama a tratar ou declarar `throws`.\
Uma unchecked exception herda de `RuntimeException` e pode subir pelas camadas sem essa obrigação.\
O defeito de `ClassificacaoIndicativaException` era a falta de um handler, não o fato de ela ser checked.\
O Spring também consegue tratar uma checked exception; usei `RuntimeException` para seguir o padrão das outras exceções do projeto.\
A correção da resposta foi adicionar um método no `GlobalExceptionHandler` para capturá-la e devolver HTTP 422.\
Assim, a mensagem com a idade do usuário e a classificação do conteúdo chega ao cliente em `erro`.

### 4. Sobrescrita vs sobrecarga (Aula 7)

Sobrescrita acontece quando a subclasse usa a mesma assinatura de um método herdado.\
Sobrecarga acontece quando o nome é igual, mas os parâmetros são diferentes.\
Em `Serie`, existia `calcularPrecoAluguel(double desconto)`, enquanto `Conteudo` tinha o método sem parâmetros.\
Por isso chamadas feitas por uma referência `Conteudo` usavam o preço padrão de R$ 9,90.\
Ao remover o parâmetro, `Serie` passou a sobrescrever o método e calcular R$ 4,90 por temporada.\
O `@Override` faz o compilador avisar se a assinatura não corresponder ao método da classe mãe.

### 5. Onde blindar o objeto? (Aulas 3, 4 e 13)

O construtor deve impedir que um objeto já nasça inválido, como um conteúdo com duração negativa.\
O setter também deve proteger a mesma regra, pois o valor pode mudar depois e o JSON usa setters ao montar o objeto.\
No aluguel, a validação deve ficar no próprio método de negócio antes do débito dos créditos.\
Foi assim que impedimos saldo negativo, conteúdo indisponível e acesso fora da classificação indicativa.\
Os campos nulos de `Serie` e `Usuario` tinham outra causa: construtores incompletos e uma atribuição sem `this`.\
Validar apenas no controller não bastaria, porque outras partes do programa também podem criar ou alterar essas entidades.

### 6. Abstração e interface (Aulas 8 e 9)

`Conteudo` é uma classe abstrata porque reúne estado e comportamento comum de filme, série e documentário.\
Ela guarda título, categoria, duração, classificação e disponibilidade, que todas as subclasses possuem.\
`Promocionavel` representa uma capacidade: somente alguns tipos de conteúdo aceitam promoção.\
Se documentários ganhassem promoção, `Documentario` passaria a implementar `Promocionavel`.\
Também seria necessário implementar `aplicarPromocao`, como já ocorre em `Filme` e `Serie`.\
O controller e `calcularPrecoPromocional` permaneceriam iguais, mostrando que o polimorfismo reduz o impacto da mudança.

---

## Parte 4 — Espaço livre

O ponto mais importante desta atividade foi perceber que um projeto compilar não significa que suas regras estejam corretas. Vários defeitos só apareceram ao comparar as subclasses e seguir o fluxo controller → model → repository. As correções foram mantidas pequenas e registradas separadamente para que cada causa raiz fique visível no histórico.

Nos testes com saldo exato, uma série de 3 temporadas calculava `14.700000000000001` por causa da representação de `double`. Por isso, os cálculos de preços, promoções e saldo após o débito usam `Math.round(valor * 100.0) / 100.0` para trabalhar com centavos. Esse ajuste complementa as correções de preço e créditos.

O tratamento global de `IllegalArgumentException`, adicionado no bug03, também faz os endpoints de usuário inexistente devolverem HTTP 400 com a mensagem da exceção. O contrato do PDF não define um status específico para esse caso.

### Testes

| Cenário testado | Resultado observado |
|---|---|
| Filme comum / estreia: aluguel e promoção | R$ 9,90 / R$ 14,90; promoções de R$ 7,92 / R$ 11,92. |
| Série de 5 temporadas: aluguel e promoção | R$ 24,50; promoção de R$ 19,60. |
| Série de 3 temporadas com saldo de R$ 14,70 | Aluguel aceito; saldo zero confirmado no GET do usuário. |
| Série de 3 temporadas com saldo de R$ 14,69 | HTTP 422; saldo e disponibilidade preservados. |
| Três filmes comuns com saldo inicial de R$ 29,70 | Saldos de R$ 19,80, R$ 9,90 e R$ 0,00. |
| Documentário disponível com usuário sem créditos | Aluguel gratuito aceito; preço promocional também zero. |
| Conteúdo indisponível / classificação indicativa / falta de créditos | HTTP 409 / 422 / 422, com as mensagens das regras. |
| Duração zero e negativa nos três tipos de conteúdo | HTTP 400 com mensagem de duração; nenhum registro salvo. |
| Conteúdo inexistente / consulta por categoria | HTTP 404 com mensagem / somente conteúdos da categoria pedida. |
| Cadastro de usuário e série | ID gerado e nome preservado; dados herdados da série preenchidos. |
