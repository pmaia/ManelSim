O experimento:

	Existem três operações do BeeFS que podem ser iniciadas para servir outros usuários que não o usuário nativo da máquina. Read, Write e CopyFrom.
	O objetivo desse experimento é saber se existe alteração na potência de operação de uma máquina quando o BeeFS está executando a operação copyFrom.


1- Instrumentação:

	1.1- Modifiquei Honeycomb para logar momentos de início e fim da operação copyFrom (essa informação é mantida em memória até Honeycomb ser finalizada através de {BEEFS_HOME}/bin/beefs stop honeycomb, quando é gravada num arquivo em /tmp)
	1.2- Instalei a versão de honeycomb modificada em meu laptop*
	1.3- Instalei a mesma versão de honeycomb na minha máquina do LSD** (Só por comodidade. Não havia necessidade de usar a versão modificada aqui)
	1.4- Criei um script para capturar informações do estado da bateria, além de frequencia e utilização de CPU (utilization_power_freq.sh)
	1.5- Criei um cliente para iniciar copyFroms de X bytes em algum Honeycomb a cada Y min

2- Ensaio 01 (commit: 8aa0e0a3b2d67efe47bd5b2f70d5860f2f3b195d) - Qual a potência de operação quando não existe (quase) nada acontecendo em background e uma operação de copyFrom chega? Qual o impacto na utilização da CPU?

	2.1- Mudei as opções de gerencia de energia do meu laptop para não mudar o brilho da tela, nem desligá-la após momentos de ociosidade e fechei todas janelas exceto a de um terminal
	2.2- Iniciei o script criado em 1.4 para começar a logar a frequencia, utilização e estado da bateria
	2.3- Removi o cabo do carregador e comecei a utilizar a bateria do laptop. Dessa forma, com as informações de status da bateria eu conseguiria saber a potência de operação do laptop.
	2.4- Fiz o cliente criado em 1.5 iniciar 10 copyFroms de 50MB a cada 1 minuto. O cliente rodou na minha máquina do LSD, o alvo dos copyFrom era o honeycomb instalado na minha máquina.
	2.5- Após o término dos 10 copyFroms, finalizei o honeycomb no laptop e o script inicializado em 2.2.

	** Conclusões **: A operação não tem impacto significante na utilização da CPU, mas aumenta em ~3W a potência de operação da máquina.

2- Ensaio 02 (commit: ) - Se houver uma atividade CPU bound, o aumento na potência de operação continua fixo em ~3W? 

3- Ensaio 03 (commit: ) - Se houver uma atividade IO bound, ainda existe aumento na potência?



* Notebook Inspiron 15:
	Processador: Intel® Core™2 Duo P8700 (2.53 GHz, 3 MB L2 cache, 1066 MHz FSB))
	Memória: 4GB DDR2 800MHz (2x2GB)
	Tela LCD:
	Placa de Video: Integrada Intel Graphics Media Accelerator X4500HD
	Disco Rígido: SATA de 500GB (5400RPM)
	Adaptador de Rede: Intel WiFi Link 5100 Half Mini Card (802.11agn)
	Linux 3.0.0-20-generic-pae (Ubuntu 11.04)
** 
