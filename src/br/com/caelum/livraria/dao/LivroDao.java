package br.com.caelum.livraria.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import br.com.caelum.livraria.modelo.Livro;

@Stateless
public class LivroDao {
	

	@PersistenceContext
	private EntityManager manager;
	
	
	public void salva(Livro livro) {
		manager.persist(livro);
	}
	
	public List<Livro> todosLivros() {
		return manager.createQuery("select l from Livro l", Livro.class).getResultList();
	}

	public Livro livrosPeloNome(String nome) {
		
       TypedQuery<Livro> query = (TypedQuery<Livro>) this.manager.
    		   createNativeQuery("SELECT * FROM livro WHERE titulo = '"+ nome + "'", Livro.class);
		
		return (Livro) query.getSingleResult();
	}
	
}
