package interceptador;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class LogInterceptor {


	@AroundInvoke
	public Object intercepta (InvocationContext context) throws Exception {

			long tempoMillisInicio = System.currentTimeMillis();
			
			Object objetoQualquer = context.proceed();
			
			System.out.println("TEMPO GASTO PELA APLICAÇÃO NA EXECUÇÃO: " 
			+ (System.currentTimeMillis() - tempoMillisInicio));
		
			
			return objetoQualquer;
		

	}
	
}
