package br.com.caelum.livraria.webservice;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import br.com.caelum.livraria.dao.LivroDao;
import br.com.caelum.livraria.modelo.Livro;

@WebService
@Stateless
public class LivrariaWS {
	
	@Inject
	private LivroDao livroDAO;

	@WebResult(name="livro")
	public Livro getLivrosPeloNome(@WebParam(name="titulo")String nome){
		
		System.out.println("PROCURANDO PELO LIVRO DE NOME: "+ nome);
			
		return livroDAO.livrosPeloNome(nome);
	}
	
}
