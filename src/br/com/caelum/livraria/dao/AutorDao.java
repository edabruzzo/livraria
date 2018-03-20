package br.com.caelum.livraria.dao;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import br.com.caelum.livraria.modelo.Autor;


@Stateless
public class AutorDao {

	@Inject
	private Banco banco; // = new Banco();
	
	@PostConstruct
	void aposCriacao() {
		
		System.out.println("FOI CRIADA UMA INSTÂNCIA DE AUTOR DAO");
		
	}
	

	public void salva(Autor autor) {
		
		System.out.println("TEMPO ATUAL DA APLICAÇÃO : " + System.currentTimeMillis());
		/*try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		banco.save(autor);
		System.out.println("TEMPO ATUAL DA APLICAÇÃO APÓS SALVAR O AUTOR : " + System.currentTimeMillis());

	}
	
	public List<Autor> todosAutores() {
		return banco.listaAutores();
	}

	public Autor buscaPelaId(Integer autorId) {
		Autor autor = this.banco.buscaPelaId(autorId);
		return autor;
	}
	
}
