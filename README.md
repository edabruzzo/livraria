# livraria
PROJETO ALURA - EJB - O poder do Java EE

********************************************************************
THIS PROJECT IS JUST A PERSONAL INITIATIVE WITH DIDATIC PURPOSES
********************************************************************


SERVIDOR DE APLICAÇÃO : JBOSS - WILDFLY - 11.0.0 - FINAL

IDE UTILIZADA NO DESENVOLVIMENTO - ECLIPE JEE - OXYGEN
Eclipse Java EE IDE for Web Developers.
Version: Oxygen.1a Release (4.7.1a)

EJB - 3.0
PRIMEFACES 4.0
ALL-THEMES -1.0.10
JSF - 2.0


INTEGRAÇÃO DO JPA COM POOL E DATASOURCE


**************************************************************

PARA REGISTRO DO DRIVER DO MYSQL NO WILDFLY 11:
https://desenvolvo.wordpress.com/2013/08/27/configurando-datasource-no-jboss-wildfly-com-mysql/

PARA CONFIGURAÇÃO DOS DATASOURCES :

http://www.adam-bien.com/roller/abien/entry/installing_oracle_jdbc_driver_on

TAMBÉM É NECESSÁRIO CONFIGURAR NO STANDALONE.XML A REFERÊNCIA AO DATASOURCE PADRÃO 'EXAMPLEDS', ALTERANDO PARA livrariaDS

https://stackoverflow.com/questions/46258244/how-to-correctly-use-datasource-on-wildfly-10-1-0


**************************************************************



Revisão
No último capítulo vimos como funciona o ciclo de vida dos Session Beans. Aprendemos a configurar um método callback de criação através da anotação @PostConstruct, além de configurar o pool de objetos pelo servidor JBoss AS. Vimos que Session Beans são automaticamente thread safe, e todo acesso é sincronizado.

Nos exercícios, deixamos todos os DAOs como Session Bean Stateless para em seguida injetá-los nos beans.

Na classe Banco usamos um Session Bean especial, o Singleton Bean. Ele é especial pois garante automaticamente que existirá apenas uma instância deste bean.

Todos os beans foram injetados pelo EJB Container através da anotação @Inject.

Nesse capítulo vamos focar na persistência, ou seja, na integração do JPA com o EJB. O foco então é o uso do JPA dento do EJB Container. Para aqueles que gostariam de aprender o JPA mais a fundo, aconselhamos assistir ao treinamento específico de JPA 2 na plataforma Alura.

Injetando o EntityManager
Até agora usamos a classe Banco para simular um banco de dados, mas chegou a hora de realmente persistir os dados. O primeiro passo é injetar o EntityManager; interface principal da especificação JPA. Para tanto, onde usávamos Banco, passaremos a usar o EntityManager.

O EntityManager possui métodos de alto nível para trabalharmos com objetos. Para salvar o autor podemos usar o método persist():

manager.persist(autor);
Para listar todos os autores, basta executar uma query:

manager.createQuery("select a from Autor a", Autor.class).getResultList();
Por último, podemos procurar um autor pelo id:

manager.find(Autor.class, autorId);
Pronto, a classe AutorDao já está compilando, agora só falta ajustar a anotação de injeção de dependência. Quando injetamos um EntityManager não podemos utilizar a anotação @Inject. Nesse caso, o Contexts and Dependency Injection (CDI), outra especificação com o foco na injeção de dependência, buscaria o EntityManager. No entanto não encontraria o objeto e causaria uma exceção. Como o EJB Container administrará o JPA, é preciso usar uma anotação especifica do mundo EJB, nesse caso @PersistenceContext:

@PersistenceContext
EntityManager manager;
Isso fará com que o EJB Container injete o EntityManager. Mas qual banco de dados vamos utilizar e qual é o endereço desse banco? Para tudo isso realmente funcionar, temos que definir algumas configurações sobre o banco de dados.

Configuração do banco de dados
O primeiro passo é copiar o arquivo persistence.xml que faz parte do JPA. Já preparamos o arquivo para você e está disponível dentro dos resources, basta copiar a pasta META-INF para a pasta src do projeto livraria.

O arquivo persistence.xml possui algumas configurações específicas do mundo JPA como, o nome da unidade da persistência, o provedor de persistência e as entidades do projeto - todas elas explicadas no treinamento JPA 2 da plataforma Alura.

Também há algumas propriedades sobre a conexão com o banco de dados, usuário, senha e o driver connector utilizadas. O problema é que não devemos configurar os dados da conexão dentro do persistence.xml. Quem é responsável por fornecer a conexão é o EJB Container! É um serviço que o servidor disponibilizará para a aplicação.

A única coisa que deve ser feita dentro do persistence.xml é configurar o endereço do serviço. Para isso, existe a configuração <jta-data-source>. Vamos deixar o endereço ainda com interrogações para entender como configura-lo primeiro.

Usando o datasource
Como já falamos antes, é responsabilidade do servidor fornecer a conexão com o banco de dados. Uma conexão é feita através de um driver connector, por isso precisamos registrar o driver do banco MySQL como módulo no JBoss AS.

Dentro da pasta resources nos downloads já temos o módulo preparado, que consiste de um arquivo XML e um JAR do connector. Esses dois arquivos devem ser copiados para a pasta modules do JBoss AS.

Internamente o JBoss AS organiza seus módulos em pacotes, por isso devemos navegar para a pasta modules/com. Dentro da pasta com criaremos uma nova pasta mysql e dentro dela uma pasta main. Dentro da pasta main colocaremos o arquivo XML e o JAR (hierarquia final das pastas: jboss/modules/com/mysql/main)

Ao iniciar o JBoss AS, ele já carregará o novo módulo. Agora falta dizer ao JBoss AS que esse módulo representa um driver connector. Isso é feito no arquivo de configurações standalone.xml.

Vamos abrir o XML dentro de um editor de texto qualquer e procurar pelo elemento <drivers>. Dentro desse elemento vamos copiar a configuração do driver que já está disponível na pasta resources.

<driver name="com.mysql" module="com.mysql">
    <xa-datasource-class>
        com.mysql.jdbc.jdbc2.optional.MysqlXADataSource
    </xa-datasource-class>
</driver>
A configuração do driver refere-se ao módulo definido anteriormente e fornece um nome para esse driver, além de especificar o nome da classe.

Por último, falta configurar o componente que no JavaEE chamamos de DataSource. Em uma aplicação mais robusta, é boa prática utilizar um pool de conexões. Cabe ao pool gerenciar e verificar as conexões disponíveis. Como existem várias implementações de pool no mercado, o JavaEE define um padrão que se chama DataSource. Podemos dizer de maneira simplificada que um DataSource é a interface do pool de conexões.

Podemos ver no arquivo XML que até já existe um datasource dentro do JBoss AS. Nele podemos ver o min e max de conexões definidos, além do nome do driver responsável e os dados sobre o usuário e senha do banco.

Agora só precisamos definir o nosso próprio datasource. Isso também já está preparado dentro da pasta resources. Basta copiar e colar a definição do datasource para o arquivo standalone.xml.

<datasource jndi-name="java:/livrariaDS" pool-name="livrariaDS"
    enabled="true" use-java-context="true">

    <connection-url>jdbc:mysql://localhost:3306/livraria</connection-url>
    <driver>com.mysql</driver>
    <pool>
        <min-pool-size>10</min-pool-size>
        <max-pool-size>100</max-pool-size>
        <prefill>true</prefill>
    </pool>
    <security>
        <user-name>root</user-name>
        <password></password>
    </security>
</datasource>
Repare que aquelas configurações do persistence.xml estão dentro do datasource agora. O servidor JBoss AS então criará o pool de conexões disponilizando-o para as aplicações. A única coisa que as aplicações precisam saber é o endereço do serviço. Em nosso caso o endereço é java:/livrariaDS.

Vamos copiar e colar este endereço no persistence.xml. Pronto, a única informação que a aplicação precisa saber agora é que está acessando um datasource que se chama livrariaDS. Os detalhes da configuração estão totalmente desacoplados da aplicação.

Preparação do banco de dados
Vamos reiniciar o servidor e ficaramos atentos à saída no console para perceber possíveis problemas de configuração.

Para nossa surpresa o JBoss AS jogou um exceção. A mensagem Unkown Database indica que o MySQL não conhece o banco livraria. Esquecemos de preparar o MySQL.

Para resolver o problema vamos abrir um terminal e abrir uma conexão com o MySQL. Em nosso caso basta digitar:

mysql -u root
Uma vez estabelecida a conexão do terminal com MySQL podemos criar e testar o banco:

create database livraria;
use livraria;
show tables;
Como acabamos de criar o banco, ainda não há nenhuma tabela. Voltando ao Eclipse, vamos novamente iniciar o JBoss AS.

Dessa vez o servidor iniciou sem problemas. Até podemos observar no console que as tables foram criadas no banco.

Com o terminal ainda aberto testaremos rapidamente se as tabelas realmente existem. Basta repetir o comando show tables. O terminal mostrará as tabelas corretamente.

Testando a persistência
Chegou a hora de testar a aplicação pela interface web.

No navegador, após o login, podemos ver que o combobox dos autores está vazio. Isso faz sentido pois não cadastramos ainda nenhum autor. Vamos verificar o cadastro de autores e inserir alguns autores.

Agora, no combobox aparecem corretamente os autores que indicam a execução sem problemas. Vamos verificar o console, nele aparece o SQL gerado pelo JPA.

A próxima tarefa é alterar o UsuarioDao, que ainda usa a classe antiga. Mas isso ficará para os exercícios.

PARA REGISTRO DO DRIVER DO MYSQL NO WILDFLY 11:
https://desenvolvo.wordpress.com/2013/08/27/configurando-datasource-no-jboss-wildfly-com-mysql/

PARA CONFIGURAÇÃO DOS DATASOURCES : 

http://www.adam-bien.com/roller/abien/entry/installing_oracle_jdbc_driver_on

TAMBÉM É NECESSÁRIO CONFIGURAR NO STANDALONE.XML A REFERÊNCIA AO DATASOURCE PADRÃO 'EXAMPLEDS', ALTERANDO PARA livrariaDS

https://stackoverflow.com/questions/46258244/how-to-correctly-use-datasource-on-wildfly-10-1-0



************************************************************************************************************************************************

No último capítulo, vimos como usar o JPA com EJB. O uso dentro de uma aplicação foi bastante simples, basta injetar o EntityManager para utilizar os métodos que acessam a persistência com JPA.

Vimos também que o JPA delega uma boa parte das configurações para o servidor JavaEE. É o JBoss quem fornece uma DataSource e que encapsula os detalhes da configuração do driver e do pool de conexão.

Nos exercícios, migramos todos os nossos DAOs para usar o EntityManager. Os DAOs foram injetados nos Beans através da anotação @Inject, sempre seguindo as boas práticas de injeção de dependências.

Vamos subir uma vez o servidor para mostrar que tudo continua funcionando. Enquanto o JBoss está iniciando, abrimos o terminal para se conectar com o MySQL. Todas as tabelas foram criadas com sucesso. E, ao executar um "select" na tabela Usuario, podemos ver que já existe um usuário cadastrado com o login admin e a senha pass.

Caso não tenha criado esse usuário basta executar o SQL seguinte:

insert into Usuario(login, senha) values('admin','pass');
Por fim, vamos testar a aplicação pela interface gráfica. Após o login, vamos voltar ao Eclipse para verificar o console. Podemos ver o SQL gerado no console.

Como não tem nenhum autor, nem livro cadastrado, vamos inserir um pela interface. O Autor será o Paulo Silveira, e o livro será "Arquitetura Java".

Novamente, voltando para o console do Eclipse, podemos analisar o SQL gerado. Agora aparecem também os inserts gerados pelo JPA. Ótimo, o JPA está configurado e utilizado corretamente.

Transação JTA
Já conseguimos manipular os dados no banco através da nossa aplicação. Mas, como isso funcionou já que em nenhum momento nos preocupamos com o gerenciamento de uma transação? Isso é importante pois o MySQL precisa ter uma transação para realmente gravar os dados. A resposta é que o EJB Container automaticamente abriu e consolidou a transação sem ser necessário deixar isso explicito no código. Mais um serviço disponível para os EJBs!

Isso é bem diferente caso utilizemos o JPA fora de um EJB Container. Nesse caso seria necessário gerenciar a transação na mão, ou seja, usando os método begin() e rollback(). Podemos testar isso rapidamente, basta tentar usar o método getTransaction() de EntityManager para chamar begin() e commit():

//é ilegal chamar getTransaction() dentro do EJB Container
manager.getTransaction().begin();
manager.persist(autor);
manager.getTransaction().commit();
Vamos testar esse código uma vez e reiniciar o servidor JBoss. Após o login, navegamos para página de cadastro dos autores. Ao salvar um autor, aparece no console do Eclipse uma exceção. Repare que a mensagem da exceção deixa bem claro que não podemos utilizar o método getTransaction().

Então, como e qual transação devemos utilizar? No final já tem alguma transação rodando! Um dica está no persistence.xml. Ao abrir e revisar a declaração do endereço do data-source, podemos ver o elemento jta-data-source. JTA significa Java Transaction API que é um padrão JavaEE que se preocupe com o gerenciamento da transação dentro de um servidor JavaEE. Para ser mais correto, o JTA é coordenador de transação e é ele quem vai coordenar a transação do JPA.

Antes de continuar e aprender mais sobre JTA, vamos apagar as chamadas do getTransaction() dentro do DAO.

Gerenciamento da transação com JTA
O JTA, então, é a forma padrão de gerenciar a transação dentro do servidor JavaEE e já funciona sem nenhuma configuração. Este padrão se chama CONTAINER MANAGED TRANSACTION (CMT).

Podemos deixar a nossa intenção explicita e configurar o gerenciamento pelo container. Para tal existe a anotação @TransactionManagement que define o tipo de gerenciamento da transação, no nosso caso CONTAINER:

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER) //opcional
public class AutorDao {
Essa configuração é totalmente opcional e serve apenas para fins didáticos. Igualmente, podemos deixar explicito o padrão da configuração (atributo) para cada método. Para isso existe a anotação @TransactionAttribute:

@TransactionAttribute(TransactionAttributeType.REQUIRED) //opcional
public void salva(Autor autor) {
REQUIRED significa que o JTA garante uma transação rodando quando o método é chamado. Se não tiver nenhuma transação, uma nova é aberta. Caso já tenha uma rodando, a atual será utilizada. De qualquer forma, sempre é preciso ter uma transação (REQUIRED).



É importante ressaltar que o tipo de gerenciamento CONTAINER e o atributo REQUIRED já é o padrão adotado para um Session Bean, então não é necessário configurar. Ou seja, ao testar e republicar a aplicação, tudo deve continuar funcionando. Vamos acessar a aplicação e fazer um teste. Após login, podemos cadastrar um autor sem problemas. Ótimo.

TransactionAttribute
Através da anotação @TransactionAttribute, temos acesso a outras configurações. A primeira a testar é o MANDATORY. MANDATORY significa obrigatório. Nesse caso, o container verifica se já existe uma transação rodando, caso contrário, joga uma exceção. Ou seja, quem faz a chamada deve abrir uma transação.

Vamos testar isso. Depois de ter republicado a aplicação pelo Eclipse, podemos acessar a interface web. Após login, vamos diretamente para o cadastro de autores. No entanto, ao cadastrar um autor, recebemos uma exceção. No console do Eclipse aparece o nome e a mensagem da exceção. Nesse caso foi lançado um EJBTransactionRequiredException.

Então, quando e como devemos utilizar o MANDATORY?

Normalmente, os DAOs não são o lugar ideal para abrir uma nova transação. Ao usar um DAO é preciso ter uma transação rodando. Quem faz a chamada precisa se preocupar com isso e abrir uma transação para o DAO funcionar.

Serviços como Transaction boundary
Repare que na nossa aplicação são os BEANs que usam os DAOs, por exemplo o AutorBean. O problema aqui é que os BEANs não são EJBs (não são Session Beans) e por isso não têm acesso ao JTA.



Para resolver isso vamos criar uma classe intermediária, uma classe AutorService que fica entre os Beans e os DAOs. A classe AutorService também será um Session Bean e responsável por abrir uma nova transação. É ela quem recebe um AutorDao injetado e delega a chamada:



Na classe AutorService, vamos primeiro injetar o AutorDao::

@Stateless
public class AutorService {

    @Inject AutorDao dao;
O método adiciona(..) recebe um autor e delega a chamada para o DAO:

public void adiciona(Autor autor) {
    dao.salva(autor);
}
Nesse método poderiam ficar mais regras ou chamadas de regras de negócios. É muito comum ter essa divisão de responsabilidade entre bean, serviço e DAO em um projeto real. O bean possui muito código relacionado ao JSF (view), o serviço é o controlador na regra de negócio e o DAO possui o código de infra-estrutura.



Por fim, falta refatorar a classe AutorBean, pois ela usa ainda AutorDao. Vamos injetar o AutorService e renomear a variável dao. Vamos também gerar o método todosAutores() que faz apenas a delegação para o DAO.

Como já falamos, o padrão TransactionAttribute é REQUIRED, ou seja, ao chamar o método adiciona(..) será aberta, automaticamente, uma nova transação.

Revisando, o bean recebe a chamada da tela, delega para o serviço que abre a transação e delega para o DAO.

Vamos atualizar a aplicação e testar pela interface web. Depois de ter passado pelo login, vamos tentar cadastrar mais um autor. Ao cadastrar, nenhuma exceção foi lançada e o autor aparece na tabela. Dessa vez, o AutorDao foi chamado dentro de uma transação existente. Ótimo.

Outros atributos
Além do REQUIRED e MANDATORY, há outros atributos disponíveis. O REQUIRES_NEW indica que sempre deve ter uma nova transação rodando. Caso já exista, a transação atual será suspensa para abrir uma nova. Caso não tenha nenhuma rodando, será criada uma nova transação.

Outro atributo NEVER é quem indica que jamais deve haver uma transação em execução. Isso pode ser útil para métodos que obrigatoriamente devem ser executados sem contexto transacional. Vamos testar isso uma vez para mostrar o funcionamento. Novamente vamos publicar e acessar a aplicação. Ao cadastrar o autor, recebemos uma exceção. A mensagem deixa claro que a configuração do TransactionAttributeType não permite a chamada dentro da transação.

Além disso, existem outros atributos como SUPPORTS e NOT_SUPPORTED que veremos nos exercícios.

Gerenciamento da transação programaticamente
O gerenciamento da transação pelo container é uma das vantagens do EJB e sempre deve ser a maneira preferida de se trabalhar. Contudo existe uma outra forma, parecida com aquela mostrada baseado no EntityManager. Essa forma permite o controle programaticamente, chamando begin() ou commit() na mão.

Para o EJB Container aceitar o gerenciamento da transação programaticamente, é preciso reconfigurar o padrão. Ou seja, ao invés de usar CONTAINER na anotação TransactionManagement usaremos BEAN, porque o Session Bean vai gerenciar a transação (também é chamado BEAN MANAGED TRANSACTION). Assim também podemos apagar a anotação @TransactionAttribute que não faz mais sentido.

Para realmente gerenciar a transação, é preciso injetar um objeto com este papel. Para este propósito existe a interface UserTransaction do JTA. Basta injetar o objeto através da anotação @Inject:

@Inject UserTransaction tx;
UserTransaction possui os métodos clássicos relacionados com o gerenciamento da transação como begin(), commit() e rollback(). O problema é que exige um tratamento excessivo de exceções checked que poluem muito o código.

Vamos colocar as chamadas dos métodos begin() e commit() dentro de um try-catch. O Eclipse ajuda nessa tarefa e gera automaticamente o bloco de tratamento. No nosso exemplo, para simplificar o entendimento, vamos capturar qualquer exceção, ou seja, fazer um catch(Exception):

public void salva(Autor autor) {

    //...
    try {
        tx.begin();
        manager.persist(autor);
        tx.commit();
    }catch(Exception e) {
        e.printStackTrace();
    }
    //...
}
Falta testar a aplicação. Vamos publicar as alterações e acessar a aplicação pelo navegador. O objetivo é cadastrar mais um autor para ver se o gerenciamento da transação realmente continua funcionando. Ao inserir, o autor aparece na tabela, o que indica que foi cadastrado com sucesso.

******************************************************************************************************************************************



Vamos testar alguns tipos de gerenciamento de transações dentro de um servidor JEE?
1) Apenas com objetivos didáticos, abra a classe AutorDao e anote-a com @TransactionManagement(TransactionManagementType.CONTAINER) para definirmos explicitamente que quem controla nossas transações é o container.

    @Stateless
    @TransactionManagement(TransactionManagementType.CONTAINER)  // Opcional
    public class AutorDao {
        ...
    }
2) Ainda na classe AutorDao vamos deixar explicito o padrão de configuração de transações para cada método. Faça isso anotando o método salva() com @TransactionAttribute(TransactionAttributeType.REQUIRED):

@TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void salva(Autor autor) {
        ...
    }
ATENÇÃO: Vale ressaltar que ambas configurações feitas são opcionais, visto que são os padrões adotados pelo container. Então, ao republicar e testar a aplicação, tudo deve continuar funcionando normalmente.

3) Podemos entender melhor o controle de transações implementando a divisão de responsabilidades proposta no vídeo do capítulo. Comece substituindo o TransactionAttributeType.REQUIRED por TransactionAttributeType.MANDATORY), assim, o container irá verificar se há uma transação aberta, caso contrário, lançará uma exception do tipo EJBTransactionRequiredException. Teste isso reiniciando o servidor após a alteração e tentando adicionar um novo autor (olhe no console do eclipse).

4) Agora, vamos colocar a responsabilidade de criar a transação em um outro EJB que não seja o DAO, visto que os DAOs não são o melhor lugar para isso. Crie uma classe, no pacote br.com.caelum.livraria.bean, chamada AutorService que ficará entre o AutorBean e AutorDao. Transforme essa classe em um EJB Stateless e injete o EJB AutorDao nela.

5) Crie em AutorService um método adiciona() que recebe um Autor e delega para o DAO a tarefa de salvar, e um outro método chamado todosAutores() que também fará apenas a delegação para o DAO.

@Stateless
public class AutorService {

    @Inject
    AutorDao dao;

    public void adiciona(Autor autor) {
        this.dao.salva(autor);
    }

    public List<Autor> todosAutores() {
        return this.dao.todosAutores();
    }
}
6) Precisamos agora refatorar a classe AutorBean para utilizar o novo EJB de serviços AutorService. Sendo assim, ao invés de injetarmos AutorDao, passaremos a injetar AutorService. Não podemos esquecer de refatorar os métodos que usavam o atributo dao para passar a usar o atributo service:

@Model
public class AutorBean {
    ...

    @Inject
    private AutorService service; // AutorDao dao; // = new AutorDao();

    ...
    public void cadastra() {
        // this.dao.salva(autor);

        this.service.adiciona(autor);
        this.autor = new Autor();
    }

    public List<Autor> getAutores() {
        // return this.dao.todosAutores();

        return this.service.todosAutores();
    }
}
7) Faça o Full Publish da aplicação, adicione um novo autor e veja se a exception do teste anterior será novamente lançada. O que você acha que aconteceu?

********************************************************************************************






Será que tem problemas em trabalhar com transações de forma programática? Vamos fazer um teste!
1) Abra a classe AutorService e anote o metodo adiciona() com @TransactionAttribute(TransactionAttributeType.REQUIRED). Depois, faça o método lançar uma exception após chamar o método salva() do DAO:

@TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void adiciona(Autor autor) {
        this.dao.salva(autor);
        throw new RuntimeException(
                "[ERRO] Erro lançado para testar o rollback da transação.");
    }
2) Dentro de AutorDao, modifique o método salva() para que ele suspenda a transação corrente, abra uma nova, realize o commit, encerre a nova transação e reative a transação que havia sido suspensa no início do processo. Para isso, basta anotar o método com @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW):

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void salva(Autor autor) {
        ...
    }
3) Faça o Full Publish da aplicação no JBoss AS e depois limpe o console do Eclipse para ver melhor as mensagens de saída da nossa aplicação.

4) Vamos testar o cadastro de Autor. Tente incluir um novo autor! Ao clicar em salvar parece que nada acontecer, certo? Olhe o console do Eclipse. Foi lançada a exception que criamos no AutorService, sendo assim, a transação foi encerrada com erro, um rollback foi realizado e nada foi gravado no banco, certo?

5) Abra o terminal de comandos do MySQL e consulte todos os elementos da tabela Autor no banco Livraria:

$ mysql -u root
mysql> use Livraria;
mysql> select * from Autor;
6) E aí? Mesmo a aplicação tendo apresentado um erro no console e realizado rollback na transação, por que você acha que o autor foi inserido no banco assim mesmo?




************************************************************************************************



No último capítulo falamos sobre o gerenciamento da transação. Vimos que o EJB Container usa o JTA (Java Transaction API) para o gerenciamento. O JTA é apenas um coordenador de transações que já vem com o servidor de aplicação.

O uso do JTA na aplicação é transparente e não exige muita configuração. Por padrão, qualquer chamada ao Session Bean já é transacional. Vimos que a anotação @TransactionAttribute permite configurar detalhes sobre o gerenciamento. Por exemplo, podemos definir que sempre é preciso ter uma nova transação (REQUIRED) ou que ela é obrigatoria (MANDATORY).

Também vimos que é possível gerenciar a transação programaticamente através do objeto UserTransaction, algo que deve ser evitado ao máximo já que o gerenciamento pelo container é simples e atende a maioria dos cenários.

Lidando com exceções
Já percebemos que podem acontecer exceções durante a execução da aplicação, mas como o container EJB lida com elas? Além disso quais são as formas que o desenvolvedor tem para mitigar um problema?

Vamos imaginar que dentro do DAO, além de cadastrar o autor no banco, também seja feito uma chamada para um serviço externo (um web service por exemplo). Nessa comunicação a rede pode falhar ou o serviço pode ficar desligado temporariamente. Esse são problemas que não dependem da aplicação.

Ou seja, nesse caso inevitavelmente vai ocorrer uma exceção. Para simular este problema, vamos lançar uma exceção no fim do método salva() da classe AutorDao:

throw new RuntimeException("Serviço externo deu erro!");
Vamos republicar a aplicação e acessá-la pela interface, algo nada novo pra nós. Depois do login, vamos testar o cadastro de autores que executa justamente o método que causa a RuntimeException.

O resultado não é uma surpresa. No console do Eclipse aparece o Stacktrace, ou seja, a pilha de execução com a exceção em cima dela. Podemos ver que nossa exceção foi quem causou o problema.

Ainda no console vamos subir mais um pouco, quase no início. Aí podemos ver que a nossa exceção foi "embrulhada" em uma outra do tipo EJBTransactionRollbackException. O nome indica que foi feito um rollback da transação.

É importante deixar isso claro, pois antes de lançar a exceção já usamos o JPA para persistir o autor. Sem rollback o autor estaria salvo no banco. Para ter certeza disso, vamos verificar o MySQL no terminal. Nele executaremos um select para verificar a tabela Autor. Como já esperávamos, não foi salvo o autor por causa do lançamento da exceção.

Exceções da Aplicação
Vamos comentar a RuntimeException dentro da classe AutorDao e abrir a classe AutorService. Em uma aplicação real, essas classes de serviços são utilizadas para coordenar as chamadas de regras de negócios.

É claro que ao chamar alguma regra também podem aparecer alguns problemas. Alguns deles são previstos e farão parte do negócio. É comum que uma validação falhe e um valor não seja salvo, pois uma regra específica não permite. Repare que este tipo de problema não é relacionado com a infraestrutura e sim com o domínio da aplicação.

Vamos simular isso uma vez e causar uma nova exceção, mas agora uma exceção do tipo LivrariaException:

throw new LivrariaException();
Como essa classe ainda não existe no projeto, o Eclipse reclama e o código não compila. Podemos facilmente resolver isso gerando a classe com a ajuda da IDE. Basta digitar ctrl+1 na linha com o erro de compilação e o Eclipse abre o diálogo para a criação da classe.

Repare também que a classe LivrariaException já estende a classe Exception. Vamos confirmar o diálogo para criar a classe. Na nova classe o Eclipse ainda mostra um alerta em amarelo, que não representa um erro de compilação e é irrelevante para o projeto.

Voltando para a classe AutorService, surge agora um outro problema. Ao lançar a exceção LivrariaException é preciso deixar o tratamento explícito. Essa exceção, diferente do exemplo anterior, é do tipo checked, ou seja, é necessário o uso do try-catch ou throws.

No nosso caso vamos colocar o throws na assinatura do método. Isso significa que agora a classe AutorBean não compila mais, já que a chamada do serviço causa uma exceção checked. Faremos a mesma coisa usando na assinatura do método o throws. Pronto, tudo está compilando.

Vamos testar a aplicação, full publish para atualizar o JBoss e depois abrir o navegador. Após o login, novamente testaremos o cadastro de autores. Vamos tentar salvar o autor Mauricio Aniche.

Já podemos ver que o autor não aparece na tabela da interface web. Como esperado, no Eclipse aparece a exceção igual ao exemplo anterior. Ao analisar o console podemos ver a LivrariaException, mas dessa vez ela não foi "embrulhada" dentro de uma EJBTransactionRollbackException.

Para ter certeza, vamos verificar o banco de dados. Novamente selecionaremos todos os autores da tabela Autor. Para nossa surpresa o autor foi salvo! Mesmo sendo lançado uma exceção na pilha de execução, foi feito um commit na transação!

Ao atualizar a interface web, podemos ver que realmente aparece o autor. Como, então, o container lida com as exceções?

System e Application Exceptions
Vimos dois comportamentos diferentes do container referentes a exceção. O primeiro exemplo foi uma exceção do tipo unchecked que causou um rollback, e o segundo exemplo usou uma exceção checked que não causou rollback.

Pelo ponto de vista do container, o primeiro exemplo representa uma System Exception, algo grave e imprevisto. System Exception sempre causam rollback. Além disso, aquele Session Bean que lançou a exceção é invalidado e retirado do pool de objetos.

O segundo exemplo representa uma Application Exception. Que é uma erro que pode acontecer durante a vida da aplicação e é relacionado ao domínio. Por isso não causa rollback e nem invalida o Session Bean.

Por padrão, qualquer exceção unchecked é uma System Exception e qualquer exceção checked é uma Application Exception. Isso é o padrão do EJB Container, mas como já vimos anteriormente, esse padrão pode ser reconfigurado.



Configurando Application Exceptions
Vamos abrir a classe LivrariaException e deixar explícito que ela é uma Application Exception. Para isso usaremos a anotação @ApplicationException que possui atributos para redefinir o comportamento referente a transação. Vamos fazer uma configuração para que essa Application Exception cause sim um rollback:

@ApplicationException(rollback=true)
public class LivrariaException extends Exception{

}
Como sempre, vamos testar o novo comportamento. Ao cadastrar um autor pela interface web é lançado uma LivrariaException. Novamente a exceção aparece no console do Eclipse e repare também que essa exceção não foi embrulhada. Até aqui é tudo igual. No entanto, ao verificar o banco de dados, percebemos que o autor não foi salvo, ou seja, foi feito um rollback da transação.

Por fim, uma vez declarado a LivrariaException como @ApplicationException, podemos deixar ela unchecked. Isso significa que não precisamos estender a classe Exception e sim RuntimeException. Assim, o compilador não obriga o desenvolvedor a fazer um tratamento explicito da exceção. Podemos, então, apagar aquelas declarações throws na assinatura dos métodos no AutorBean e no AutorService.
