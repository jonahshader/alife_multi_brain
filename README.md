# alife_multi_brain
Test bed program for reinforcement learning neural net model development.


![Alt Text](https://github.com/jonahshader/alife_multi_brain/blob/master/images/sb_creature.gif)
"Soft Body Creature" being evaluated

![Alt Text](https://github.com/jonahshader/alife_multi_brain/blob/master/images/food_creature.gif)
"Food Creature" being trained and evaluated



Uses custom neural net model that allows for cycles to exist, which can be structured in ways to form memory cells and other memory-holding substructures. This cyclic network model is similar to what is used in resevoir computing, but the weights in the model I created can all be trained whereas in resevoir computing, only the output layer can be trained. 

Two reinforcement learning tasks are implemented in this program. One is called Soft Body Creature, which defines a creature (agent) as a soft body where the point masses are grippers which have controllable friction. The grippers are connected with muscles, which are dampened springs with controllable target lengths. These are controllable via a neural network. The goal of the creature is to move as far away from its spawn location as possible. The creature has no sensors, so it relies on the cyclic network for generating patterns that can operate the creatures components in such a way that it moves. The creature's body structure is also mutatable. It can randomly gain or lose grippers and muscles. 

The next is called Food Creature, which defines a creature that has a 2D grid of greyscale sensors that see the world around it. The world is a 2D grid with "food" tiles spawned in random positions. The creature's goal is to consume this food while minimizing its own velocity. The creature's outputs are velocity and eat/move. Eat/move restricts the creature to be either eating or moving. This forces the creature to make an intelligent decision on when to move and when to eat, as opposed to doing both all the time (which is a less difficult task). Here's a longer video: https://drive.google.com/file/d/1Uxtw06mWh5fRZaoIOOuIpHuvcO4IeyfV/view?usp=sharing

Recently, I have been working with a research group that has developed a new kind of neuron for spiking neural networks. I have been using this software to explore the performance of these neurons in the various reinforcement tasks and cyclic network models. 
