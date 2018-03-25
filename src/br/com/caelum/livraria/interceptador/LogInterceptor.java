package br.com.caelum.livraria.interceptador;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class LogInterceptor {


	@AroundInvoke
	public Object intercepta (InvocationContext context) throws Exception {

			long tempoMillisInicio = System.currentTimeMillis();
			
			Object objetoQualquer = context.proceed();
			
			String nomeMetodoInterceptado = context.getMethod().getName();
			
			String nomeClasseInterceptada = context.getTarget().getClass().getSimpleName();
			
			System.out.println("ESTE É O INTERCEPTADOR - "
					+ "TEMPO GASTO PELA APLICAÇÃO NA EXECUÇÃO " 
			 + "DO MÉTODO " +
					nomeMetodoInterceptado + " DA CLASSE " + nomeClasseInterceptada + " : "
					+ (System.currentTimeMillis() - tempoMillisInicio) + " ms");
		
			return objetoQualquer;
		

	}
	
}
