package br.com.caelum.livraria.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import br.com.caelum.livraria.modelo.Usuario;

@Stateless
public class UsuarioDao {


	@PersistenceContext
	private EntityManager manager;
	
	
	
	public Usuario buscaPeloLogin(String login) {
	
		Usuario usuario = new Usuario();
		
		try{
			
			usuario = (Usuario) this.manager
		
			.createQuery("select u from Usuario u where u.login=:pLogin")
			.setParameter("pLogin", login).getSingleResult();
			
		}catch(NoResultException nre){
			System.out.println("NÃO ENCONTRADO O USUÁRIO!");
			criaPrimeiroUsuario();
			
		} return usuario;
			
    }
	
	public void criaPrimeiroUsuario() {
		System.out.println("CRIANDO USUÁRIO !");
		Usuario usuario = new Usuario();
		usuario.setLogin("admin");
		usuario.setSenha("admin");
		
		manager.persist(usuario);
	}
	
}
