package br.com.caelum.livraria.dao;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.caelum.livraria.modelo.Autor;
import interceptador.LogInterceptor;


@Stateless
public class AutorDao {

	@PersistenceContext
	private EntityManager manager; 	
	
	
	
	@PostConstruct
	void aposCriacao() {
		
		System.out.println("FOI CRIADA UMA INSTÂNCIA DE AUTOR DAO");
		
	}
	
	
	/*este método vai ser interceptado pela classe LogInterceptor 
	a interceptação pode ser feita na classe também ou em métodos
	como aqui é um array de classes interceptadoras,
	poderíamos ter várias classes interceptando este método*/
	
	@Interceptors({LogInterceptor.class})
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
