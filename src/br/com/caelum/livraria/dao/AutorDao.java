package br.com.caelum.livraria.dao;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.caelum.livraria.modelo.Autor;


/*os métodos  desta classe serão interceptados pela classe LogInterceptor 
a interceptação pode ser feita na classe ou em métodos diretamente
como aqui é um array de classes interceptadoras, poderíamos ter várias 
classes interceptando */
/*A configuração de interceptor pode ser feita por arquivo xml 
 * No caso, para testar vamos criar o arquivo ejb-jar.xml na pasta WEB-INF
 */
//@Interceptors({LogInterceptor.class})
@Stateless
public class AutorDao {

	@PersistenceContext
	private EntityManager manager; 	
	
	
	
	@PostConstruct
	void aposCriacao() {
		
		System.out.println("FOI CRIADA UMA INSTÂNCIA DE AUTOR DAO");
		
	}
	
	

	public void salva(Autor autor)  {
		
		manager.persist(autor);
		
	}
	
	public List<Autor> todosAutores() {
		return manager.createQuery("select a from Autor a", Autor.class).getResultList();
	}

	public Autor buscaPelaId(Integer autorId) {
		Autor autor = this.manager.find(Autor.class, autorId);
		return autor;
	}
	
}
