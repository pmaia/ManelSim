from SimPy.Simulation import *
from random import Random,expovariate,uniform

class G: # globals
	Rnd = Random(12345)
	RepairPerson = Resource(1)

class MachineClass(Process):
    TotalUpTime = 0.0 # total up time for all machines
    NRep = 0 # number of times the machines have broken down
    NImmedRep = 0 # number of breakdowns in which the machine
    UpRate = 1/1.0 # breakdown rate
    RepairRate = 1/0.5 # repair rate
    NextID = 0 # next available ID number for MachineClass objects
    NUp = 0 # number of machines currently up

    def __init__(self):
        Process.__init__(self)
        self.StartUpTime = 0.0 # time the current up period stated
        self.ID = MachineClass.NextID   # ID for this MachineClass object
        MachineClass.NextID += 1
        MachineClass.NUp += 1 # machines start in the up mode

    def Run(self):
		while 1:
			self.StartUpTime = now()
			print now()
			yield hold,self,G.Rnd.expovariate(MachineClass.UpRate)
			print now()
			MachineClass.TotalUpTime += now() - self.StartUpTime
			MachineClass.NRep += 1
			if G.RepairPerson.n == 1:
				MachineClass.NImmedRep += 1
			yield request,self,G.RepairPerson
			yield hold,self,G.Rnd.expovariate(MachineClass.RepairRate)
			yield release,self,G.RepairPerson

def main():
    initialize()
    for I in range(2):
    	M = MachineClass()
        activate(M,M.Run())
    MaxSimtime = 100.0
    simulate(until=MaxSimtime)
    print "proportion of up time:", MachineClass.TotalUpTime/(2*MaxSimtime)
    print "proportion of times repair was immediate:", float(MachineClass.NImmedRep)/MachineClass.NRep

if __name__ == "__main__":
	
	main()