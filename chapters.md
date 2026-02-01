# Summary

(Generated from segmented processing)

## Introduction to Deep Learning

[00:00:00.000 → 00:00:08.600] So hello everybody, this is the first content lecture in deep learning and we'll talk about the background of deep learning and why do we need deep learning at all in this lecture?
[00:00:09.360 → 00:00:18.400] Now why is deep learning such a hot topic right now and really I think most of you are actually here because you heard that deep learning is this thing to do now?
[00:00:18.400 → 00:00:22.760] Why is this? Well at its core the promise really of deep learning is
[00:00:22.760 → 00:00:36.360] is quite simple collect enough data, label it and voila you get what seems to be like a magic black box, predict the capable of well quite astonishing tasks very often that kind of human level performance levels.
[00:00:36.360 → 00:00:50.560] But let's add a caveat my this mainly holds true in supervised learning right now and this is crucial to understand that where deep learning and big data converges the big money basically follows.
[00:00:50.560 → 00:01:05.160] So this has basically made the field not only highly competitive but also unfortunately sometimes very stressful especially for us and you people working in this environment and sometimes even quite toxic when it comes to be who was first doing certain things.
[00:01:05.160 → 00:01:16.560] But the story really doesn't end there deep learning isn't just roses and rainbows it has also sometimes the outside technologies like where you have probably all heard of them deep fakes and
[00:01:16.560 → 00:01:27.160] potential for adversarial attacks to change data and outcomes to influence decision making basically they pose serious ethical and security concern right now.
[00:01:27.160 → 00:01:42.760] So as we marvel at the capabilities of deep learning it's also crucial to keep in mind this is quite a complex topic it is sometimes difficult to understand sometimes it looks like just an engineering effort that's quite straightforward.
[00:01:42.760 → 00:01:48.760] But keep in mind all of these complexities that also go way beyond the engineering that is involved.
[00:01:48.760 → 00:02:10.760] Now the fundamental architecture of deep learning systems or learning systems in general comprise these three components first there is usually some sort of feature extractor you want to identify patterns in the data to inform further bounce dream tasks in this context for example an image analysis is often becomes a convenient convolution neural network.
[00:02:10.760 → 00:02:20.760] And this is this part how to extract features this is really what deep learning is about and this is what we will discuss most in the coming to us real lectures.
[00:02:20.760 → 00:02:29.760] The feature extractor here is just responsible for capturing hierarchical patterns in the data like edges textures or more complex shapes of feature feature combinations.
[00:02:29.760 → 00:02:34.760] This is really what allows the network to understand what what it's looking at.
[00:02:34.760 → 00:02:43.760] And usually the feature extractor is followed by some bottleneck or task specific hats something that does for example prediction a classification.
[00:02:43.760 → 00:02:49.760] And this component is tailored to the problem that we are usually trying to solve.
[00:02:49.760 → 00:03:03.760] It doesn't really matter what the problem is if you want to classify cats or dogs or extract language the only difference I would probably make is either if you project data down to a single value doing some classification regression tasks.
[00:03:03.760 → 00:03:09.760] Or if you try to generate or order in code your input with these setups.
[00:03:09.760 → 00:03:12.760] Well, there are also components around these setups.
[00:03:12.760 → 00:03:30.760] There is parameter optimization of course this is also where some of the magic happens how to best optimize algorithms like with algorithms like for example has the gradient descent and find tuning how the overall model looks like how many how much data do you put in to really learn something how do you just the learning rates.
[00:03:30.760 → 00:03:39.760] This is also some some of the skills we need to learn to see how these hyper parameters can be achieved.
[00:03:39.760 → 00:03:48.760] Now there are lots and lots of success stories you have probably all heard of them I would recommend you to have a look around on the Internet.
[00:03:48.760 → 00:03:56.760] I've collected just a few interesting videos probably some of them can be considered historical by now but.
[00:03:56.760 → 00:04:25.760] But all of them are reasonably interesting I think in this context where deep learning really played a role that led to a breakthrough and power time shift in these fields and you probably all remember the hotdog detector in the famous TV series Silicon Valley and this probably brings some of the peculiarities of the early times of deep learning on the tableware everybody probably thought this is some sort of magic and can do a lot but if you remember that hotdog detector was only able to.
[00:04:25.760 → 00:04:33.760] To detect that it's a hotdog or no hotdog but it was sold as a general sort of food detector or I don't know.
[00:04:33.760 → 00:04:52.760] Now the big question is why are you actually here you had already program machine learning classes statistics classes where you learned about neural networks and and how neurons can be designed to build a universal approximator that should actually be able to do all of these tasks maybe only with one hidden layer.
[00:04:52.760 → 00:05:12.760] But the point is traditional networks were where for the time ground breaking for many applications but really when it came to image and text analysis really high dimensional data they had their role backs one of the primary issues is specifically dealing with the sheer complexity and dimensionality and size of say image data.
[00:05:12.760 → 00:05:26.760] We're really talking about huge pixel grids large images our smartphones do probably millions of pixels up to giga pixel images which can be overwhelming for a little neural networks.
[00:05:26.760 → 00:05:39.760] Let's probably consider for a moment the way humans interpret images we know quite a lot about the human visual system and we naturally recognize hierarchies and patterns and there are stages actually in the human visual systems that go from.
[00:05:39.760 → 00:05:43.760] Large scale patterns down to kind of more.
[00:05:43.760 → 00:05:57.760] Intricative tasks traditional neural networks well they have a day they don't have an innate understanding of spatial hierarchies of hierarchies at all that are presented in these sort of complex data like images.
[00:05:57.760 → 00:06:16.760] Another critical challenge is really translation invariance for instance if a traditional network where you basically take the image line it up in a long vector and put that through your network yeah if if a traditional network is trained to recognize for example a cat in the bottom left corner of an image it may not easily recognize that the same cat.
[00:06:16.760 → 00:06:39.760] The same cat if it appears somewhere in say the top right corner simply because you do not preserve spatial consistency in this long vectorizations of your input data in essence traditional networks they lack the flexibility to really adopt to objects appearing in different locations maybe magical times here and there and they they don't have a sense for hierarchies.
[00:06:40.760 → 00:06:55.760] And well in traditional networks say if you have one hidden layer then overfitting really becomes a significant concern for especially with image data the risk amplifies because simply because of the high number of parameters all of these many many connections in your traditional network.

## Limitations of Traditional Neural Networks

[00:06:40.760 → 00:06:55.760] And well in traditional networks say if you have one hidden layer then overfitting really becomes a significant concern for especially with image data the risk amplifies because simply because of the high number of parameters all of these many many connections in your traditional network.
[00:06:55.760 → 00:07:19.760] So that are involved in these images and and these challenges really led to the rise of of convolution neural networks where hierarchies and spatial priors and so on are introduced and they were designed specifically to address these limitations also with for example image data in mind where locality translation invariance and some the player big factor.
[00:07:19.760 → 00:07:48.760] Let's also try to understand this a little bit more because the mathematical underpinning of neural networks is really known as the universal approximation theorem that theorem just states that if you have say a function to this phi to be a non constant bounded and monotonically increasing function what this means is that phi is a sensible activation function like for example used in neural network in CNN's like the railroad which we're introducing a bit.
[00:07:48.760 → 00:08:11.760] So if you think about the universal approximation zero and have a function that can protect data in a non linear way but but bounded and monotonically increasing then for a positive error and any continuous function defined on a compact subset then the theorem actually issues us that we can approximate this function to within an error epsilon.
[00:08:12.760 → 00:08:36.760] What this means really is that the theorem tells us that we've just one hidden layer so one non linear monotonically increasing bounded protection function we can approximate any function any any function that functions if you have just one hidden layer everything is connected to everything we can approximate every function there is.
[00:08:36.760 → 00:08:46.760] And that's a quite big claim and well of course there are some practical challenges it we can approximate this function within a certain error bound yeah the epsilon error.
[00:08:46.760 → 00:09:00.760] And in practice of course this epsilon can be very large making the approximation probably less useful sometimes probably even falling back to something more linear which I could have done without introducing billions and billions of parameters.
[00:09:01.760 → 00:09:25.760] And second problem that we encounter is what's known as the curse of dimensionality where the computational and spatial complexity really exponentially increases with the number of dimensions you feed in the bigger that hidden layers the bigger your input the more of these connections you have the more you run into the problem of the curse of dimensionality which is something we'll discuss just in the next slide in more detail.
[00:09:25.760 → 00:09:53.760] So to take these issues the typical solution is to break up the problem into many many smaller problems which is essentially what multiple layers can do in deeper neural networks and this hierarchical decomposition really allows us to learn complex more complex functions in an easier way each of these sub layers now approximate only a part of the function extracts certain features for example and solves a small sub problem of the entire big problem.
[00:09:55.760 → 00:10:23.760] So what is this curse of dimensionality well a cornerstone a cornerstone really understanding the challenges of machine learning is this course of dimensionality so let's just start acknowledging the determine self sounds very dramatic right but it kind of complex conveys the complications that that really arise when you deal with high dimensional data and if I say high dimensional data I really mean anything beyond 10 more dimensions.
[00:10:23.760 → 00:10:51.760] So there is I have posted the link here on the slide there are some very nice discussions on stack exchange for example about what the mathematical properties are I would recommend you to look into those and and also what you see on this slide here is some of these slides have exclamation mark and they give you a hint that this is relevant for for the exam and that you should probably revise more carefully over those then some are more for illustration purposes where I have omitted this.
[00:10:53.760 → 00:11:03.760] So let's look at the first one of the questions about the question mark. But for a complete picture please look at all of the slides and I hope to also give you a kind of nice round up through these videos.
[00:11:04.760 → 00:11:20.760] So let's let's talk about the curse of dimensionality so first you have that function approximation thing so what can this be imagine you have a function say let's call it F that maps data from some sort of the dimensional space to one real number.
[00:11:20.760 → 00:11:30.760] Now your aim is to really approximate this function with a specific level of accuracy which we usually denote denote as as epsilon.
[00:11:31.760 → 00:11:47.760] And this epsilon really brings us to the concept of sample explosion now to do this approximation the number of samples required to achieve this level of accuracy epsilon really grows exponentially with the number of dimensions I have in each sample with the.
[00:11:47.760 → 00:12:03.760] So mathematically this is expressed as the number of samples you need to approximate a liqueous continuous function to sufficient to even accuracy epsilon is in the order the order of epsilon expected error to the power of minus D.

## The Curse of Dimensionality

[00:12:04.760 → 00:12:10.760] So if I want an error of 0.1 then I have 0.1 to the power of how many dimensions you have in your input samples.
[00:12:11.760 → 00:12:27.760] So let's consider for a moment of one dimensional space perhaps you're trying to approximate a line if you if you need 10 samples for epsilon level accuracy in one day you'll need 100 samples into the right we have now epsilon to the minus two.
[00:12:28.760 → 00:12:36.760] And potentially 1000 in 3D the sample requirement really explodes as dimensions increase and this was now just an example approximating a single line.
[00:12:37.760 → 00:12:41.760] And this really this example explosion makes the task super computationally burdensome.
[00:12:42.760 → 00:12:47.760] So why should we actually care in reality we probably don't have that many samples.
[00:12:48.760 → 00:12:53.760] Well this exponential growth in required samples really poses a challenge.
[00:12:54.760 → 00:13:02.760] And training models on high dimensional data if you just have input vector for five or so on then you can directly approximate your function steps general statistics.
[00:13:03.760 → 00:13:11.760] But if you put in an image and you've probably 36 million input pixels and then your data is really high dimensional so the more features.
[00:13:12.760 → 00:13:25.760] So more dimensions really mean more features and without enough samples models cannot overfit or fail to recognize really underlying patterns that make your function approximation general.
[00:13:25.760 → 00:13:28.760] So that it also works with new data and not only your training data.
[00:13:29.760 → 00:13:40.760] It's really summary the curse and the curse of dimension that you refers to at least computational data specific challenges that we encounter when we.
[00:13:41.760 → 00:13:43.760] Look at really high dimensional data like images.
[00:13:44.760 → 00:13:54.760] The curse of dimensionality is sort of difficult to understand to comprehend and to develop intuition for it because our human brain usually ends at three dimensions in a Euclidean space.
[00:13:54.760 → 00:13:57.760] Maybe you can at time to have it intuitive to some degree.
[00:13:58.760 → 00:14:00.760] But I you cannot tell me that you understand the 10 dimensional space.
[00:14:01.760 → 00:14:03.760] Now what you really has good intuition about it.
[00:14:03.760 → 00:14:10.760] So let's probably look into another few examples and let's talk about pizzas and watermelons.
[00:14:11.760 → 00:14:13.760] So let's let's start with a pizza analogy.
[00:14:14.760 → 00:14:18.760] It said that more than half of a pizza lies near its edge.
[00:14:21.760 → 00:14:23.760] So outside of this shaded area here.
[00:14:24.760 → 00:14:30.760] Interestingly, the width of this crust is only 18% of the diameter of the pizza.
[00:14:30.760 → 00:14:34.760] It seems a bit weird half of your pizza isn't the crust.
[00:14:34.760 → 00:14:38.760] But only if you measure diameter only 18% is the crust.
[00:14:39.760 → 00:14:41.760] Now consider a thick skin fruit like a watermelon.
[00:14:41.760 → 00:14:43.760] Such fruits the maturity of the volume.
[00:14:43.760 → 00:14:45.760] This is not three dimensional objects.
[00:14:45.760 → 00:14:47.760] The maturity of the volume is actually in the skin.
[00:14:48.760 → 00:14:50.760] Not the juicy part inside.
[00:14:51.760 → 00:14:52.760] So what's the relevance?
[00:14:52.760 → 00:14:56.760] Both examples illustrate really counterintuitive geometric principle.
[00:14:57.760 → 00:15:03.760] In higher dimensions, the maturity of the mass of volume of an object can reside near its boundaries.
[00:15:05.760 → 00:15:06.760] So how do we generalize this?
[00:15:07.760 → 00:15:17.760] We can really model various objects like the board of a map, the crust of a pizza or the skin of a fruit by supposing that their basic shapes have been uniformly shrunk by some factor.
[00:15:18.760 → 00:15:19.760] Let's call it out.
[00:15:19.760 → 00:15:22.760] Let's shrink that whole thing like the pizza the melon by some factor alpha.
[00:15:23.760 → 00:15:28.760] The crust or rain is essentially what lies between these two similar shapes.
[00:15:28.760 → 00:15:32.760] We go a little bit smaller, everything is between these two shapes.
[00:15:33.760 → 00:15:39.760] So in higher dimensions, spaces like those we often encounter in deep learning, this geometric inside has really consequences.
[00:15:39.760 → 00:15:48.760] For example, when we are working with high-dimensional data, most of our training samples, they lie actually close to the edge of that space.
[00:15:48.760 → 00:15:49.760] Most of them.
[00:15:50.760 → 00:16:05.760] This phenomenon has implications for things like clustering, outlet detection and the training, really training of neural networks because we don't really use the space, the volume in that space, but have everything, all of the information at the boundaries of these high-dimensional spaces.
[00:16:12.760 → 00:16:13.760] Let's write this down.
[00:16:14.760 → 00:16:23.760] So if we unpack the surprising behavior to exhibit, particular with regards to volume, this is something that might help us with some intuition.
[00:16:24.760 → 00:16:26.760] So first, how do high-dimensional volumes look like?
[00:16:26.760 → 00:16:30.760] We begin by understanding, say, volumes in an end-dimensional space.
[00:16:30.760 → 00:16:40.760] When we scale a shrinking object like before, and just look at what's at its boundary, with a factor alpha, if we shrink and scale it with alpha, it's volume,
[00:16:40.760 → 00:16:46.760] will it becomes a factor of A to the power of N times the volume of the original shape?
[00:16:46.760 → 00:16:47.760] Okay?
[00:16:47.760 → 00:16:52.760] So this is straightforward enough, but as we'll see, the implications are quite profound.
[00:16:52.760 → 00:16:58.760] Let's think about the concept of a rind, or a scheme of that space.
[00:16:59.760 → 00:17:11.760] So if we visualize the rind as the boundary of the shell that surrounds our object or our space, to determine the volume of this rind, we just subtract the volume of the shrinking object from its original.
[00:17:11.760 → 00:17:12.760] Right?
[00:17:12.760 → 00:17:15.760] Say, 1 minus A to the power of N.
[00:17:15.760 → 00:17:22.760] The rind represents really not the difference between the original and the shrinking volume in behaves rather unexpectedly in high-dimensional spaces.
[00:17:22.760 → 00:17:26.760] Let's see, let's look at the rind's growth rate, if we increase dimensions.
[00:17:26.760 → 00:17:31.760] So we can determine how the volume of this rind grows as we adjust the shrinking factor.
[00:17:31.760 → 00:17:45.760] Mathematically, this growth rate is defined by here d times, number of dimensions times 1 minus alpha to power of N, and this is approximately, this is equal to minus N times alpha to power of N minus 1, dA.
[00:17:45.760 → 00:17:54.760] So this is my growth rate, if I change the factor alpha when I shrink my water medium, for example.
[00:17:54.760 → 00:17:57.760] So, but the change here, here it gets really interesting.
[00:17:57.760 → 00:18:08.760] When we start with no shrinking, that is alpha equals 1, and gradually begin to decrease alpha to rind volume grows at an initial rate of N times.
[00:18:08.760 → 00:18:13.760] Right? That rind's volume grows at an initial rate of N times.
[00:18:13.760 → 00:18:22.760] So this essentially means the rind's volume surges N times faster than the rate at which we are shrinking the object.
[00:18:22.760 → 00:18:27.760] This behavior is a bit counterintuitive, but really important to understand what's happening here.
[00:18:27.760 → 00:18:34.760] So if we shrink just a little bit, the volume gets N times faster bigger.
[00:18:34.760 → 00:18:41.760] So in high-dimensional, even the new changes in distance can have magnified effects on volumes.
[00:18:41.760 → 00:18:48.760] A tiny change in one dimension can lead to a substantially larger change in the volume of that space.
[00:18:48.760 → 00:18:57.760] So as an example, in 2, 0, 3D spaces, which you're familiar with, the interior volume is alpha to power of N times the original volume.
[00:18:57.760 → 00:19:01.760] Yeah, alpha to the N times the original volume.
[00:19:01.760 → 00:19:05.760] This is the interior volume.
[00:19:05.760 → 00:19:08.760] So that's change.
[00:19:08.760 → 00:19:15.760] Yeah? In higher dimensions, a really, really small change in distance can lead to large changes in volume.
[00:19:15.760 → 00:19:24.760] So for machine learning, this has massive implications on data distributions, sampling and generalization.
[00:19:24.760 → 00:19:32.760] It's still not probably 100% clear. So let's dive into something more related to our pizza.
[00:19:32.760 → 00:19:38.760] Let's ask the question, how the salami behave on a high-dimensional pizza.

## Shift Invariance and Equivariance

[00:19:32.760 → 00:19:38.760] Let's ask the question, how the salami behave on a high-dimensional pizza.
[00:19:38.760 → 00:19:42.760] I want to have as much salami as possible on the pizzas I get.
[00:19:42.760 → 00:19:46.760] So I'll ask the question, how does it actually behave on a high-dimensional pizza?
[00:19:46.760 → 00:19:53.760] Well, it may sound like we're talking about dinner plans. This is actually a great way to understand how data also behaves in high-dimensions.
[00:19:53.760 → 00:19:55.760] The concept is really crucial here.
[00:19:55.760 → 00:19:59.760] So imagine a pizza, where the salami is uniformly spread out.
[00:19:59.760 → 00:20:03.760] Should you rather eat the inside or should you rather eat the crust?
[00:20:03.760 → 00:20:10.760] We want to really figure out how much of this salami is close to the boundary of the pizza in such a high-dimensional space.
[00:20:10.760 → 00:20:14.760] Let's think about that briefly in the concept of half-links.
[00:20:14.760 → 00:20:22.760] That term is just borrowed from radioactive decay because the growth is exponentially looking at.
[00:20:22.760 → 00:20:27.760] So we can also look at this as a half-length and say half-time.
[00:20:27.760 → 00:20:29.760] Let's call this also half-length.
[00:20:29.760 → 00:20:31.760] So let's define a half-length for alpha.
[00:20:31.760 → 00:20:34.760] To make our pizza half is the original volume.
[00:20:34.760 → 00:20:36.760] We shrink it by a factor of alpha.
[00:20:36.760 → 00:20:42.760] The mathematical representation is just alpha to the power of n is a half, like here.
[00:20:42.760 → 00:20:44.760] n is just the dimensions of our pizza.
[00:20:44.760 → 00:20:49.760] Two dimensions plug in two and we have alpha to the power of two equals one half.
[00:20:49.760 → 00:20:56.760] If we source this equation, we get alpha equals two to the power of minus one over n.
[00:20:56.760 → 00:21:03.760] If we just calculate this log here, it's approximately one minus 0.7 over n.
[00:21:03.760 → 00:21:12.760] So for two-d pizza, the half length is alpha, if we put in two into n, is 1 minus 0.35.
[00:21:12.760 → 00:21:19.760] So about 80% of the pizzas that are diameter will contain half the area and half the salami.
[00:21:19.760 → 00:21:23.760] For 3d pizza, now it's getting interesting.
[00:21:24.760 → 00:21:26.760] For 3d pizza, now it's getting interesting.
[00:21:26.760 → 00:21:30.760] For 3d pizza, alpha is 1 minus 0.23.
[00:21:30.760 → 00:21:36.760] Half the volume lies just within 12% of its diameter from the boundary.
[00:21:36.760 → 00:21:42.760] So half of all of the salami is distributed in only 12% from the diameter from the boundary.
[00:21:42.760 → 00:21:47.760] So let's think big. Let's think at 350-dimensional pizza.
[00:21:47.760 → 00:21:50.760] In this case, alpha is greater than 98%.
[00:21:50.760 → 00:22:01.760] That means in 350 dimensions, almost all of the salami, 98% of the salami is within 1% of the pizzas diameter from its boundary.
[00:22:01.760 → 00:22:05.760] The core question really is what proportion of the dataset is near the boundary.
[00:22:05.760 → 00:22:09.760] This becomes relevant when we think about how data is spread.
[00:22:09.760 → 00:22:18.760] If our data is uniformly spread across dimensions, understanding how it behaves near the boundary is crucial for model training and generalization.
[00:22:18.760 → 00:22:23.760] So what we have discussed now this really holds unless the data is strongly clustered.
[00:22:23.760 → 00:22:28.760] If your data points tend to stick together in certain regions, these calculations might not be quite accurate.
[00:22:28.760 → 00:22:32.760] If you have more uniform distributed data, then it's more relevant.
[00:22:32.760 → 00:22:41.760] But making the core if the data is uniformly distributed or clustered for that, you need to actually first project them into a dimensions and see how your distances behave.
[00:22:41.760 → 00:22:44.760] Is it learnable or not?
[00:22:45.760 → 00:22:57.760] Another way to think about this is the idea that in higher dimensions, in dimensions, most Echlidean distances between observations will be nearly the same.
[00:22:57.760 → 00:23:09.760] If I measure the distance between two points in Echlidean way, the square, all of the dimensions and then take the square root, the length of the vector between them, they will be almost the same, all of them, in very high dimensions.
[00:23:09.760 → 00:23:14.760] And there also be close to the diameter of the region and closing them.
[00:23:14.760 → 00:23:20.760] So when we say very close, we are actually taking, we're talking in terms of one over N.
[00:23:20.760 → 00:23:23.760] So as dimension increases, this closeness becomes more pronounced.
[00:23:23.760 → 00:23:27.760] And an important qualifier is really without strong clustering again.
[00:23:27.760 → 00:23:32.760] If we have uniform data and you just characterize this data, the above statement really, really holds true.
[00:23:32.760 → 00:23:37.760] So the distances between these points will be more or less the same.
[00:23:37.760 → 00:23:51.760] You can also say that, yeah, so in this figure, there's another way to represent this and illustrate this, this tells us how the volume of a hypersphere changes relatively to the hypercube that contains it.
[00:23:51.760 → 00:23:58.760] So if I draw a cube around here, what is the area that's between the sphere and the cube?
[00:23:58.760 → 00:24:11.760] And the core takeaway is really that what we get from the 2D and 3D case or even higher dimensions that the volume of the hypersphere starts to shrink significantly, becoming almost negligible compared to the volume of the hypercube that encloses it.
[00:24:11.760 → 00:24:20.760] This has really implications because that means that most of our data now lives outside in these boundary regions.
[00:24:20.760 → 00:24:25.760] The figure also shows how the number of corners in a hypercube exponentially grow with dimensionality.
[00:24:25.760 → 00:24:35.760] And this is then telling you, you know, how much how many corners you have really that can contain volume and includes this hypersphere.
[00:24:35.760 → 00:24:42.760] So in an 8D hypercube, for example, that that one has how many corners, 256 corners.
[00:24:42.760 → 00:25:09.760] And this can really demonstrate somehow how actually your data starts to not only live in the right of that space, but the more dimensions you have, the more likely it is that all of your data each sample lives in its own corner of say that hypercube, if you approximate that space like that usually spaces more spherically, but the right each has a distinct location if you have really high dimension spaces.
[00:25:09.760 → 00:25:20.760] Well, the distance between corners of a cube, they are equal, right. So the distances, if every sample lives its lives in its own corner, the distances become almost the same vision.
[00:25:20.760 → 00:25:31.760] Damian also tried to illustrate it that way is probably the best you can get for very high dimensions. So in three dimensions, we can still kind of comprehend what's going on. All of the samples lives somewhere in this space of the enclosing hypercube.
[00:25:31.760 → 00:25:43.760] But in very high dimensions, it's very difficult to visualize you would need, if you measure the distance, you would need to go back here going back out to the other dog and so all the distances become more or less the same.
[00:25:43.760 → 00:26:06.760] So the unicube assumption with the corners is not super correct, it's probably one of the most intuitive ways to think about why distances become equal with the corners, but the unicube is in fact asymmetric and it's not the greatest approximation.
[00:26:06.760 → 00:26:20.760] To mitigate this issue, one approach is to say, roar the interval into a loop. So the unicube, we can roar this interval into a loop, a unicube in one, it would be just a line segment, so we can roll it in the loop, where the starting point zero meets at the end at point one.
[00:26:20.760 → 00:26:33.760] This creates what is actually known in higher dimensions as a detour, and in one way it just looks like this, and two becomes this kind of strip and in many dimensions becomes a thourose.
[00:26:33.760 → 00:26:46.760] And when we plot the distribution of normalized distances between different samples in this multi-dimensional space, and quite interesting pattern emerges, the normalization process sent us the histogram around a value of approximately 0.58.
[00:26:46.760 → 00:26:54.760] And now one key insight to draw really from this is that around any given point in a high dimension of torus almost all other points are nearly the same distance away.
[00:26:54.760 → 00:27:01.760] Look, if you look at the histograms, you see the higher the dimensions get, the more closer these distances between all of the points on this torus become.
[00:27:01.760 → 00:27:11.760] This is still counterintuitive, but quite important, especially since in machine learning to make a call, if this is a cat or a dog, we need to measure distance, right?
[00:27:11.760 → 00:27:17.760] Is this far away from each other than probably their different classes, is close to each other, they are probably the same classes.
[00:27:17.760 → 00:27:24.760] And again, to approximate the lips, this continuous function of r2 power of t2r, so some protection function with a certain epsilon accuracy.
[00:27:24.760 → 00:27:30.760] One needs in order of epsilon to power of minus disambles.
[00:27:30.760 → 00:27:41.760] If you put this into perspective, consider an image from whatever your smartphone with a resolution of 12 megapixels and say three color channels, this results in 36 million numerically elements.
[00:27:41.760 → 00:27:59.760] And if we want to approximate some classifier, say cats versus dogs, with an error, with an accuracy of approximately 10%, so 0.1, we would require an astounding 10 to the 36 samples to approximate this function space relatively accurately.
[00:28:00.760 → 00:28:13.760] We have a 10% error. So for context, that's a number ranging from 10 to the 78 to 10 to the 82, which is actually more atoms than they are in the known observable universe.
[00:28:13.760 → 00:28:25.760] That's how many pictures of cats and dogs you would need to accurately, with a 10% error, accurately approximate a classifier function that can classify you between cats and dogs.
[00:28:26.760 → 00:28:33.760] Another intriguing point to consider is how the volume of a circle of hyperspace changes relatively to the volume of a square or hypercube as you have just seen before.
[00:28:33.760 → 00:28:43.760] That is something you can study in depth more to see what the mathematical properties are of these spaces and how counterintuitively this really behaves.
[00:28:43.760 → 00:28:47.760] This is not just an intellectual exercise. It really has practical implications for the complexity.
[00:28:48.760 → 00:29:01.760] And when you think about feasibility of certain machine learning algorithms, in my experience, I think most of the attempts to use machine learning deep learning, especially for certain applications, which fail really fail because that data is just a similar.
[00:29:01.760 → 00:29:16.760] If you want to distinguish very, very, very similar things from each other, say, grass types and you have lots of different green pictures of a lot of grass, then it will be a very difficult program because you need really a lot of samples to generalize these functions.
[00:29:18.760 → 00:29:27.760] So let's talk about another important concept of general learning systems. This is mainly explaining terminology.
[00:29:27.760 → 00:29:32.760] So let's talk about invariance and ectrivariance. What do these things mean? They are simply then the term might suggest.
[00:29:32.760 → 00:29:36.760] So this image here contains a cat, right?
[00:29:36.760 → 00:29:46.760] And actually, I don't care where in the image this cat is located. If I look at the image, I would say no matter where the cat is located, I would classify this image as an image of a cat.
[00:29:47.760 → 00:29:53.760] And that property that regardless where the image is where the object is located, the image is called shift invariance.
[00:29:53.760 → 00:30:00.760] In shift invariance is really a property that describes the system's unchanging response when the input is shifted.
[00:30:00.760 → 00:30:04.760] So the input shifts, the output doesn't, the output stays where it is.
[00:30:04.760 → 00:30:13.760] In this context, for example, of image processing or computer vision, this means that the features of the cat in the image should be recognizable regardless of its position in the frame.
[00:30:14.760 → 00:30:23.760] This only depends on the generalizable features of that cat. The cat has a snout and ears and tail and everything these features define the cat not very positioned in the frame.
[00:30:23.760 → 00:30:29.760] This is already something a conventional neural network where everything is connected to everything and the input is linearized would struggle a lot.
[00:30:29.760 → 00:30:36.760] This property of shift invariance is quite critically in many deep learning applications, particularly in convolutional neural networks.
[00:30:37.760 → 00:30:45.760] In CNNs, we really want the network to recognize the cat, whether it's in the corner or in the center of the image and probably even find and annotate where the image is.
[00:30:45.760 → 00:30:53.760] An understanding shift invariance helps us to appreciate why certain algorithms like CNNs are actually effective at tasks like object recognition.
[00:30:54.760 → 00:31:05.760] So shift invariance really allows models to channelize better from their training data to new unseen data in this really just a term describing a property of a learning system.
[00:31:07.760 → 00:31:14.760] Shift invariance is not just a theoretical concept, but practical necessity for robust machine learning and how this is achieved.
[00:31:14.760 → 00:31:22.760] To some degree, we'll discuss in a few slides actually when we look into how CNNs can be built from some basic components.
[00:31:23.760 → 00:31:31.760] So this is what I said already. If we define shift variance more mathematically, we call say the shift itself.
[00:31:31.760 → 00:31:39.760] We denote this with an operator s with a subscript v where the redefines the vector where which we shift the object in this case, the cat around.
[00:31:39.760 → 00:31:46.760] This is this red vector, red vector. When we apply this shift operator to the input, we are essentially transforming its coordinates in the input space.
[00:31:47.760 → 00:31:57.760] This is critical for applications where the object of interest could be anywhere in the field of view, but the object itself is more or less not immutable, but more or less feature consistent.
[00:32:00.760 → 00:32:07.760] So it's probably worth at this point to clear up that CNNs are not thinking machines. They're complex functional approximators that train step by step.
[00:32:08.760 → 00:32:21.760] How to extract features from this input image to see, okay, there is still paths that would activate, for example, containing the features of cat, so they are shift invariant, but they are not thinking really about these cats.
[00:32:21.760 → 00:32:24.760] So shift invariance is a critical property.
[00:32:25.760 → 00:32:33.760] The key really is to understand that shift invariance, invariance means the input changes, the output does not change.
[00:32:34.760 → 00:32:44.760] If I shift, for example, if the output should not change, the output does not change. If I shift things around, like that object of the cat, then it will still be categorized as a cat.
[00:32:44.760 → 00:32:46.760] Now,
[00:32:47.760 → 00:33:01.760] Acre variance on the other hand means that here, acre variance on the other hand means that if I change the input, the output changes in the same way, probably in your way.
[00:33:01.760 → 00:33:07.760] Imagine a model that segments every pixel in a cat. It identifies the pixels that make the cat.
[00:33:07.760 → 00:33:13.760] It says every pixel that belongs to the cat, it says it is one, it clarifies it is one, and every other pixel is zero.
[00:33:13.760 → 00:33:24.760] This scenario, if the cat in the image moves around, the segmented output, so the identification of these pixels, they should also shift in the exact same way with the red back door read here, with the same shift operator.
[00:33:24.760 → 00:33:33.760] So simply put shift acre variance means that applying the shift operator after the function yields the same result as applying the function after the shift.
[00:33:33.760 → 00:33:41.760] Okay, just applying the shift operator after the function yields the same results as applying the function after the shift.
[00:33:42.760 → 00:33:45.760] So to put it formally, as shown here,
[00:33:47.760 → 00:33:54.760] s applied to V should be the same as f applied to s.
[00:33:54.760 → 00:33:58.760] So s applied to f should be the same as f applied to s.
[00:33:59.760 → 00:34:05.760] So this commute and you can assume that the same shift will be here.
[00:34:05.760 → 00:34:12.760] So this commutative law here is very important if the input changes the output changes in the same way.
[00:34:12.760 → 00:34:15.760] This is what acre variance means.
[00:34:15.760 → 00:34:23.760] So to put it simply, in variance is about stability. When we talk about in variance, what we mean is that no matter how the input distance from the output should remain constant.
[00:34:24.760 → 00:34:30.760] This is really, really useful for tasks where the recognition of an object is more important than the position in the input space.
[00:34:30.760 → 00:34:35.760] And if we talk about acre variance, acre variance is about consistent transformation.
[00:34:35.760 → 00:34:42.760] In an acre variance system, the transformation applied to the input is exactly the same as the transformation applied to the output.
[00:34:42.760 → 00:34:47.760] For example, if we move an object within an image, the corresponding output should move in the same way.
[00:34:48.760 → 00:34:55.760] And understanding the differences between these two can significantly affect how well our model performs depending on the task, really your tackling.
[00:34:55.760 → 00:35:04.760] So as we delve deeper into complex models and tasks, keep these concepts of invariance, acre variance, really in mind they will be crucial.
[00:35:06.760 → 00:35:16.760] Another important tool is the concept of inductive bias, which is a term coming from philosophy, or the assumptions that guide the learning process in machine learning models.

## Principles of Locality and Translation Invariance

[00:35:51.760 → 00:35:54.760] Okay, that's our first principle, locality.
[00:35:54.760 → 00:35:58.760] Second principle.
[00:35:58.760 → 00:36:00.760] Second principle is actually locality.
[00:36:00.760 → 00:36:10.760] The idea behind locality is that you shouldn't have to consider the entire image if you want to find the features of a specific object.
[00:36:10.760 → 00:36:16.760] You shouldn't have to consider far of information to understand what's happening at a specific location in your data.
[00:36:16.760 → 00:36:28.760] For instance, if you're looking at an image, the pixels immediately surrounding a location ij should provide sufficient information and sufficient context actually to determine what that area in the image contains.
[00:36:28.760 → 00:36:34.760] Did I really need to look into the left corner of the image of the cat before to determine that the cat's ear is a cat's ear?
[00:36:34.760 → 00:36:40.760] I will only need to look at the local information surrounding the cat's ear to understand that it's a cat's ear.
[00:36:40.760 → 00:36:49.760] Both of these principles are deeply embedded actually in many of the architectures we'll discuss in a bit in deep learning, particularly for convolution neural networks.
[00:36:49.760 → 00:36:54.760] So let's think about locality a little bit more.
[00:36:54.760 → 00:36:58.760] Maybe also about shifting variance, but mainly locality.
[00:36:58.760 → 00:37:10.760] Translation in variance and locality can be practically applied using one of the most common algorithms, most naive algorithms you can come up with in computer vision when you think about detecting an object.
[00:37:10.760 → 00:37:14.760] Well, what could you do? You could define your object.

## From Fully Connected to Convolutional Networks

[00:37:10.760 → 00:37:14.760] Well, what could you do? You could define your object.
[00:37:14.760 → 00:37:16.760] You say, I want to find George.
[00:37:16.760 → 00:37:20.760] I don't know if this guy is really called George, but let's say we want to find this person.
[00:37:20.760 → 00:37:33.760] How could we ideally do this? Well, we take George, slide him over the image of this entire area and the lower lower toll and then let's see if we have somewhere a match.
[00:37:33.760 → 00:37:47.760] We can just correlate this. We say we define this sub image here with George and later on we'll call such a sub image that defines what we're looking for a kernel, KXY, to analyze the local regions here.
[00:37:47.760 → 00:37:55.760] And then we come up with some sort of metric that measures how correlated this kernel is at every location at this image of the role or at all.
[00:37:55.760 → 00:38:05.760] And we can come up with this correlation formula. We can just say we sum up all of the pixels, we sum up all of the pixel differences and the smaller the value, the better.
[00:38:05.760 → 00:38:10.760] So if you have this image, this location and.
[00:38:10.760 → 00:38:15.760] So for autocorrelation, we multiplied them just together and sum up to the higher.
[00:38:15.760 → 00:38:30.760] The higher this this function gets the more these two are correlated. You really can come up with anything you like here. You can count the pixel differences or in this case, we just multiplied them with these two functions together to kernel and the image and then we measure what this varies the highest response.
[00:38:30.760 → 00:38:52.760] This specific one where you multiply together, this is called correlation, this is the correlation formula and it's kind of key part of many of the algorithms we use in computer vision and the surprising fact deal with probably get to in this lecture is that most of the convolution networks we use are actually correlation networks that use exactly that function.
[00:38:52.760 → 00:38:57.760] And why does it the case will discuss in a little bit now. Let's just think about finding George.
[00:38:57.760 → 00:39:02.760] So the first principle of translation in various tells us that it doesn't matter where in the image to person is really located.
[00:39:02.760 → 00:39:10.760] I would want to find George anywhere in this in this image. Also our algorithms should be able to detect the person no matter where they are in this image.
[00:39:10.760 → 00:39:21.760] The second principle locality comes into play here with how the kernel is actually defined. We are operating under the assumption that all the information needed to identify George is found in a local neighborhood of pixels.
[00:39:21.760 → 00:39:32.760] So we don't need to analyze the entire image at every location where we measure this correlation function. Now we only analyze it in the area of this of this kernel.
[00:39:32.760 → 00:39:39.760] And now let's see what happens if we apply this function to every pixel.
[00:39:39.760 → 00:39:49.760] So we run this kernel over it and what we get out is a correlation function like here. And as you can see George is here at the floor.
[00:39:49.760 → 00:39:56.760] So it's probably that direct way doesn't even work well even if I take a patch out of the original image.
[00:39:56.760 → 00:40:01.760] The top image here is just a heat map for function across the image.
[00:40:01.760 → 00:40:08.760] And well, this approach may work or may not work, but apparently it doesn't generalize very well.
[00:40:08.760 → 00:40:19.760] It doesn't really understand what features George is composed of. And so apparently bright and the color was more important here for this correlation function.
[00:40:19.760 → 00:40:30.760] And this is only one step. So don't expect this to work. But now let's think about something interesting.
[00:40:30.760 → 00:40:33.760] Let's think about convolutions.
[00:40:33.760 → 00:40:44.760] Convolutions are quite similar to what we just discussed correlations, but let's probably approach the whole thing from a slightly different angle.
[00:40:44.760 → 00:40:50.760] Let's go back and reconsider the classical fully connected neural network.
[00:40:50.760 → 00:40:55.760] But each input is really connected to each node. So let's recall how this works.
[00:40:55.760 → 00:40:59.760] In a fully connected network, each input is connected to each node in the subsequent layer.
[00:40:59.760 → 00:41:05.760] So it means that every single input feature influences every single neuron in the next layer.
[00:41:05.760 → 00:41:10.760] That is not quite conform with our locality principle, but it should still work quite.
[00:41:10.760 → 00:41:14.760] This is this architecture, the advantage really of this is that this is extremely flexible.
[00:41:14.760 → 00:41:19.760] In the rare cases where probably locality does not apply, this will still work.
[00:41:19.760 → 00:41:25.760] Our locality approach from before where we run the current local image will probably not work.
[00:41:25.760 → 00:41:30.760] The classical fully connected neural network can capture any kind of relationships between inputs.
[00:41:30.760 → 00:41:33.760] So it's an universal approximator.
[00:41:33.760 → 00:41:36.760] It can do it particularly well with linear relationships.
[00:41:36.760 → 00:41:45.760] However, this flexibility really comes at the cost that as we discussed already, computational complexity will be informed by the course of dimensionality.
[00:41:45.760 → 00:41:52.760] And it has quite a lot of potential for overfitting.
[00:41:52.760 → 00:41:57.760] So keep this in mind. Maybe delve a little bit further into CNNs.
[00:41:57.760 → 00:42:06.760] And probably let's deconstruct FCN a little bit more to get to an idea that eventually led to convolutional networks.
[00:42:06.760 → 00:42:14.760] So let's just reorganize our fully connected network a little bit and look at the weights of the input.

## Convolution vs Correlation

[00:42:06.760 → 00:42:14.760] So let's just reorganize our fully connected network a little bit and look at the weights of the input.
[00:42:14.760 → 00:42:18.760] So input comes in what we do is just a weight made matrix times input multiplication.
[00:42:18.760 → 00:42:20.760] So everything is connected to everything.
[00:42:20.760 → 00:42:25.760] And then we have a connection down to some some sort of input.
[00:42:25.760 → 00:42:30.760] Then a fully connected layer, each node in one layer connects to every other node.
[00:42:30.760 → 00:42:37.760] And this is this equation just tells us that where we have the weight matrices and input from the previous entry code we add them together.
[00:42:37.760 → 00:42:39.760] So just building up products here.
[00:42:39.760 → 00:42:42.760] So this this setup actually leads to a large number of parameters.
[00:42:42.760 → 00:42:45.760] And all of them are populated in this rate matrix here.
[00:42:45.760 → 00:42:51.760] And basically you have number of nodes and power of two weights here.
[00:42:51.760 → 00:42:53.760] So the size of this matrix.
[00:42:53.760 → 00:42:58.760] So again, if we consider an input of 36 million elements like on high resolution image.
[00:42:58.760 → 00:43:03.760] This is 36 million square parameters, which is just an astronomical number.
[00:43:03.760 → 00:43:08.760] If you have here the size of an image linearized linearized.
[00:43:08.760 → 00:43:15.760] So this not only makes the model computation very expensive, but also raises the likelihood of overfitting the more parameters you have.
[00:43:15.760 → 00:43:17.760] You don't form an information port like where you have to generalize.
[00:43:17.760 → 00:43:21.760] You can just memorize all of the training data and then don't don't generalize at all.
[00:43:21.760 → 00:43:27.760] If I would just store in this matrix how each of these images look like and then compare like the correlation function before every image to my training.
[00:43:27.760 → 00:43:35.760] Memory, then I can have perfect performance on the training set, but terrible performance on any test set because they don't fit.
[00:43:35.760 → 00:43:40.760] Of course, what I've memorized in the data.
[00:43:40.760 → 00:43:46.760] So how can we simplify this a little bit just one kind of deep learning,
[00:43:46.760 → 00:43:49.760] deep learning, lingo machine learning, lingo definition here.
[00:43:49.760 → 00:43:58.760] If people say something is intractable, they really mean it's hard to control a deal with and something like that for high dimensional problems is basically tractor.
[00:43:58.760 → 00:44:02.760] So you probably won't be able to compute any of these weight matrices in a reasonable time.
[00:44:02.760 → 00:44:06.760] So you can say this is intractable for reasonable size problems.
[00:44:06.760 → 00:44:11.760] It will still work for your tabular four or five dimensional data, no question, but for images, this will fail.
[00:44:11.760 → 00:44:27.760] Now, one early idea how to simplify this is was actually locality and just assume that we don't need to connect everything to everything to reduce number parameters in our model without losing the ability to really capture important features.
[00:44:27.760 → 00:44:32.760] So one way to do this is just to use vastly connected neural networks.
[00:44:32.760 → 00:44:46.760] In this input, in this architecture, each input neuron is just connected to small number of K hidden neurons rather than every other every every neuron in the previous layer.
[00:44:46.760 → 00:44:50.760] So what does this mean in number parameters? Well, now we have K times n parameters.
[00:44:50.760 → 00:44:53.760] So how many I choose to connect to? So here in this case is three.
[00:44:53.760 → 00:44:57.760] But K times n is better, much better than n squared.
[00:44:57.760 → 00:45:07.760] So we can populate our weight matrix here along the diagonal and the width of this diagonal, we define how many previous neurons you decide to connect to.
[00:45:07.760 → 00:45:19.760] So for instance, if K is three, you have an input of 36 million elements, then you only need three times 36 million parameters, which is still an astronomical number, but it's a huge reduction compared to 36 million square.
[00:45:19.760 → 00:45:27.760] So early work in this area has been done by a young account and colleagues utilizing this so called sparse connections, and to build more efficient networks.
[00:45:27.760 → 00:45:31.760] This is really a foundational concept that paved the way for convolutional networks.
[00:45:31.760 → 00:45:40.760] Now a few words on the side. Of course, this is still trained through back propagation, which you should be well familiar with from machine learning and modular perceptron training and so on.
[00:45:40.760 → 00:45:47.760] So this all of these weights are individual numbers, individual weights trains. They're just less of them and you can store them diagonally.
[00:45:47.760 → 00:45:55.760] So you put something in a measured area and then back propagates through the partial differentials and update your weights every time you see a new training setup.
[00:45:55.760 → 00:46:08.760] If you're not familiar with back propagation, please revisit this material. It's quite essential to understand to get a better intuition, actually, what's going on here because at the end of the day, you're just applying your optimizer to this and define how this error should look like.
[00:46:08.760 → 00:46:18.760] Let's get back to this. So this is already a huge reduction in parameters and but it led to action even better idea where we said, okay.
[00:46:18.760 → 00:46:28.760] How how's about we don't learn all of these parameters, but share the weights between them. So we choose three, for example, here.
[00:46:28.760 → 00:46:35.760] But instead of letting all of these weights be adopted, we just say all of these triplets here are the same.
[00:46:35.760 → 00:46:51.760] And this concept is called weight sharing. Same integration here where we say Y equals W 1 before minus 1 times X 1 before plus W 0, which is the center plus W plus 1.
[00:46:51.760 → 00:47:01.760] They're always the same way it's applied. It's not like these are in this indexed into the weight matrix. No, this red, green and blue weight. They're always the same.
[00:47:01.760 → 00:47:07.760] They are just reused for different input neurons. This is what we refer to really as weight sharing. Keep this in mind.
[00:47:07.760 → 00:47:13.760] If something is the same, it's weight sharing. And now if you look at this matrix, we only need to remember three values.
[00:47:13.760 → 00:47:21.760] So in this model, each input neuron is connected to some more number of K hidden neurons, just like in the sparsely connected network we discussed earlier.
[00:47:21.760 → 00:47:27.760] However, this time the weights are shared amongst the connections. So this results in a really dramatic reduction of parameters.
[00:47:27.760 → 00:47:37.760] For example, we have three parameters that's only three parameters we need to learn for this network. It's not three times the number of input. No, it's three parameters, one, two, three.
[00:47:37.760 → 00:47:48.760] That's all we need to learn in a network like this. And it's basically just a spars set up with shared weights.
[00:47:48.760 → 00:48:05.760] But this is the fundamental idea that made convolutional networks a powerful and efficient. We shared the weights across the neurons and then let these triplets or however you define the size of this kernel, this part here, we learned.
[00:48:05.760 → 00:48:14.760] It's kind of the same as the cross correlation idea we had before, right? So these weights here define however our kernel was defined.
[00:48:14.760 → 00:48:35.760] And in fact, really convolution and correlation are very similar. The equation for the equation for convolution and correlation, therefore, essential operations in image processing, signal processing in general, convolution in both flipping the kernel before performing the element wise multiplication and disomming up.
[00:48:35.760 → 00:48:54.760] Whereas correlation does not flip it. So in other words, convolution, we take the mirror image of the kernel across the central point before sliding across image in correlation kernel is slid across the images is without any flipping. You might be wondering why this flipping is even necessary in convolution, well, the flipping is a mathematical convenience.
[00:48:54.760 → 00:49:08.760] And this particularly useful when you're dealing with systems defined by differential equations, also signals, if you think about them, how you integrate, for example, how you sum up things in a continuous way, then it's difficult to do it from the other side.
[00:49:08.760 → 00:49:18.760] So so correlation can be used equally well. convolution is is an equally valid operation however in neural networks, especially in CNN's.
[00:49:18.760 → 00:49:47.760] The difference between convolution and correlation is often not very critical. That's really because the weights of these kernels, so these shared weights, they are learned from data whether flipped or not, if you flip them and have something pretty fine like our previously known operator, so you want to preserve your convenient mass for convolution where everything works out to be nice. For example, if you want to do say for your transforms and really do signal analysis as well, then you need to flip but in reality, if you just learn a feature extracted and met if you flip or not because your weights are anyway learned.
[00:49:47.760 → 00:50:02.760] So in practice, many deep learning libraries actually implement correlation and just called convolution is a mix between the mathematical niceties of convolution and also convention.
[00:50:02.760 → 00:50:21.760] The key take a base with both operations are really closely related and serve the same overarching course local pattern detection through shared rates. Understanding the difference is still quite interesting because the convolution itself and its mathematical properties have quite some advantages.

## Convolutional Layer Design and Computational Efficiency

[00:50:02.760 → 00:50:21.760] The key take a base with both operations are really closely related and serve the same overarching course local pattern detection through shared rates. Understanding the difference is still quite interesting because the convolution itself and its mathematical properties have quite some advantages.
[00:50:21.760 → 00:50:37.760] If you look at this great version of convolution, the essential operation in convolution neural networks as we implement them, the so we are just summing over all possible shifts from negative infinity to positive infinity.
[00:50:37.760 → 00:50:54.760] So in practice, the RS, UT and WT are of course finite and for values not defined, we assume just them to be zero. So practically speaking to some only includes the overlapping elements of two errors, errors.
[00:50:54.760 → 00:51:13.760] So in essence, we measure the similarity between UT and WT at each point T, and we slide it over this concept of discrete convolution through the backpons of our scene. Now I still would recommend you to look into how convolution works because there are two schools of thoughts here.
[00:51:13.760 → 00:51:25.760] So this is just a operation between two functions where you slide one function over the other function, multiply them with each other and then integrate some up the result, which is the essence and signals of filtering.
[00:51:25.760 → 00:51:35.760] Now if I want to smooth an image or if I want to get rid of noise in an audio signal and so on, this is the essence of filtering in signal theory.
[00:51:35.760 → 00:52:04.760] The other interesting part is why did this actually, why did this extra transition into feature extractors like correlation networks, convolution networks, I would recommend you to have, to have a look at these two YouTube videos I posted here where the convolution is very nicely correlated to probability theory and Gaussian sampling and very flipping is actually explained how you can, how you can get all possible combinations.
[00:52:04.760 → 00:52:15.760] The probability is of all possible combinations of features by just flipping around one part of your inputs or your curl and then sliding it over over your input.
[00:52:15.760 → 00:52:32.760] That is, would be very difficult with correlation alone, but that correlation really leads to the backbone, the mathematical backbone of convolutional networks because at the end of the day what you would, what you would, ideally like to predict this, the probability of an input belonging to a class.
[00:52:33.760 → 00:52:46.760] And that's where convolution were really, really useful in the mathematics backbone in practice, correlation is learned, but the convolution gives you an overarching idea of the signals that come in and go out.
[00:52:46.760 → 00:52:59.760] Now what are the properties of convolution is just the operation between two functions, their community, associative distribution rules, and the associative scalar functions.
[00:53:00.760 → 00:53:09.760] Now, why convolution again, historical reasons, the flip kernels, please watch the videos, they're very interesting, it's additional material.
[00:53:09.760 → 00:53:28.760] Cross correlation, correlation is usually implemented and learned, but it's such a powerful tool that you can have with CNNs and with weight sharing and many, many filters, which you will talk in them in a couple of slides, about which we will talk in a couple of slides, you can really construct this hierarchical feature in extraction through.
[00:53:28.760 → 00:53:31.760] Through these very basic ideas.
[00:53:31.760 → 00:53:44.760] Now here a few filters from 2D image analysis, like if you want to do edge detection, you can define a filter kernel like this, then the output will be just edges, and this is what I meant also with.
[00:53:44.760 → 00:53:52.760] With filtering operations, this is where they came from, if you do sharpening the image, Gaussian blur, you can do all sorts of operations here.
[00:53:52.760 → 00:54:01.760] The big difference in CNNs is that these filtering operations, in CNNs, are now learned through backpropagation, all of these weights are learned through backpropagation, and we have many of these filters.
[00:54:01.760 → 00:54:09.760] Each filter is responsible to react specifically to one specific feature and then the subsequent layers to feature of feature combinations.
[00:54:10.760 → 00:54:33.760] So now let's get to how to actually build convolutional neural networks with not only modern tools, but also conceptually now for practical reasons, but also to explore how data distributions actually behave.
[00:54:34.760 → 00:54:45.760] So we'll discuss a couple of things right now, and then across the lecture a few other things, we'll start with the most important part, which are convolutional layers.
[00:54:45.760 → 00:54:52.760] This is really hard to see in CNNs where filters are currently slide across the input to produce feature maps.
[00:54:52.760 → 00:55:02.760] Activation functions, we'll talk about those little later pooling layers as an essentially building block, they are all conception, not very difficult.
[00:55:03.760 → 00:55:12.760] To understand there are some tricks you can use to make them more efficient, but the key is in how all of these things come together.
[00:55:12.760 → 00:55:26.760] For the connected layers, flattened layers will discuss in a couple of slides, you can use dropout to regularize what is actually learned to make it harder to learn so things generalize better, but normalization will discuss in a few slides.
[00:55:27.760 → 00:55:32.760] And how to actually design the output of such networks.
[00:55:32.760 → 00:55:46.760] Now before we start, let's think about the input tensor again of convention neural networks, if we compare them new to neural networks, it might become very clear how locality, especially locality is implemented in CNNs.
[00:55:47.760 → 00:56:01.760] So, at a particular neural network, you'll take the input with dimensions x, y, c for say, how to be channels and simply flatten for number of RGB channels so that might be x, y, c dimension with c, the RGB and x, y, the image.
[00:56:01.760 → 00:56:08.760] And for convention neural network, we simply flatten it into a long vector of size x, y, c times one.
[00:56:08.760 → 00:56:14.760] So this is the shape of our input, this would then feed into the network for classification into say c classes.
[00:56:15.760 → 00:56:22.760] How about we have set this price about image data and this approach is quite a sub optimal.
[00:56:22.760 → 00:56:32.760] So what we mean by this is again shift in variance, if you shift an object, the intrinsic characteristics of the objects don't really change, but here they would go all over the place in this Monday vector.
[00:56:33.760 → 00:56:55.760] So for locality, in the image data, the relevant information for making a decision usually lies close, but that's not necessarily the case if you flatten out your entire vector, some pieces might just go on completely the other side, if you flatten an image into a one dimensional vector, say RGB value, RGB value RGB value RGB value, there's nothing is actually quite connected.
[00:56:55.760 → 00:57:03.760] And this locality is in a fully connected network, not naturally accounted for, but in CNNs it is.
[00:57:03.760 → 00:57:16.760] So how do CNNs do it? Well, just by keeping the locality, of course, just by keeping the input size, you notice that we have your graphical representation showing an input layer with dimensions x, y and that c.
[00:57:16.760 → 00:57:21.760] And again, the number of colors or that can be number of channels, however you define them.
[00:57:21.760 → 00:57:25.760] And I filter kernel of dimension i jk.
[00:57:25.760 → 00:57:26.760] Okay.
[00:57:26.760 → 00:57:29.760] And between them, we make a convolution.
[00:57:29.760 → 00:57:31.760] You can implement this convolution as ever you like.
[00:57:31.760 → 00:57:44.760] You can either do the sliding window or if that kernel is quite large, then it would maybe pay off to make a Fourier transform or just as many of the deep learning libraries to implement correlation, yeah, they were not the other way.
[00:57:45.760 → 00:58:00.760] So, but what does this mean for our principles for locality principle instead of flattening the input into one director, as in conventional networks, will see and maintain the original structure of the image as a 3d tensor of dimensions x, y, c.
[00:58:01.760 → 00:58:07.760] So we call these objects tensors since they behave like matrices.
[00:58:07.760 → 00:58:14.760] So this helps us really to keep the spatial relationship in the input and the kernel intact.
[00:58:14.760 → 00:58:26.760] The 3d tensor is then convolved with the smaller kernel and the idea is really the small kernel slides of the 3d input the one or the other way, preserving the local spatial relationships in the image pretty much to us we did it in this lighting window example.
[00:58:27.760 → 00:58:45.760] So, in practice, convolution is often implement as a series of dot products, we just take a patch of the input image, which matches the size of our kernel and then perform the dot product between these between the patch and the kernel.
[00:58:45.760 → 00:58:55.760] The dot product is essentially a weighted sum of the pixel values in the impunity image, where the weights are determined by the kernel and these weights in the kernel are what is learned.
[00:58:56.760 → 00:59:07.760] And the output of each of these dot products forms a single pixel in the resulting feature map, right? We do this dot products, light it over every single location and then the output is a pixel in the feature map.
[00:59:07.760 → 00:59:14.760] So by doing this, we capture it in the local spatial free features from each region in the input image and compile them into feature map.
[00:59:14.760 → 00:59:22.760] If that region is very similar to that kernel, there will be high activation, if not, there will be low activation.
[00:59:22.760 → 00:59:39.760] And this method is really highly effective and this one of the reasons why CNNs are so efficient for this sort of task, it just adheres to the principle of translation invariants and locality, capturing spatial features, which significantly reduces the number of parameters, because all you need to learn here is the number of weights in this kernel.
[00:59:39.760 → 00:59:54.760] I times J times K, this is what you need to learn for this one single layer and then again, the fifth is your feature activation map and the resulting output after this convolution will be a little bit smaller.
[00:59:54.760 → 01:00:05.760] So why are the dimensions of the output reduced well as we slide the kernel across the image and perform dot products, we're essentially aggregating information from the original pixels into new condensed values, right?
[01:00:05.760 → 01:00:17.760] And the kernel size really determines how many pixels are aggregated into each new value, which, which really defines the output, which really defines the size of the output.
[01:00:17.760 → 01:00:31.760] So this reduction dimensionality is not always a bad thing, it can actually be quite useful reducing the computation load for future layers that follow this one and can also help abstracting the higher level features in this image.
[01:00:31.760 → 01:00:36.760] Remember, the primary idea is to slide this filter over the locations in the input image to produce a feature map.
[01:00:36.760 → 01:00:44.760] And so, of course, you can't go outside, so you basically output size will be defined by half will be smaller by that kernel size.
[01:00:44.760 → 01:00:58.760] So, and you eat off the boundaries basically with that kernel in this aggregation process, which is not necessarily a bad thing.
[01:00:58.760 → 01:01:06.760] Now, if it is a bad thing for you, then you probably can fix this issue by just doing zero padding.
[01:01:06.760 → 01:01:15.760] If you add hypothetical zeros around it, so if you're outside of the input tensor and run with your kernel over it, then you can assume that there are zeros.
[01:01:15.760 → 01:01:27.760] So you add just I and J half cross, if you would not pad, but that's since the dot product, if you have a 3D and 3D tensor, convolved with which other things, the dot product will anyway collapse the dimensions in this case here into one value.
[01:01:28.760 → 01:01:51.760] So, your pad and the zeros around this will preserve the input activation that's sometimes important, not always if you want to reduce your dimensionality and say a classification program, then it's not too bad to actually accept that you with every layer you eat off half of your kernel size from each of the sides of your input tensor.
[01:01:51.760 → 01:02:07.760] Now, why would you do zero padding? Well, of course, if you eat off in every step, half of your kernel size from each of the boundaries, you're limited with how many convolutional layers you can stack at some point you'll end up in an in an input tensor that is smaller than the kernel you want to learn and then this cannot work anymore.
[01:02:07.760 → 01:02:31.760] So, if you want to get deeper, but preserve a little bit of your of your boundary, then you might also take zero padding, but that's that's the main reason why probably people do a zero padding to add more layers to actually control how the output size will be shaped for the following layers, it's not so much to preserve to preserve the size across the entire network.
[01:02:31.760 → 01:02:45.760] Now, of course, you're not only just learning one filter per layer, you would usually learn quite a number of layers and each of these learned kernels now produce its own its own activation map.
[01:02:45.760 → 01:03:05.760] So, each of them has probably a specific task finding eyes or edges or smoothing or whatsoever, but they are entirely learned, so we don't really care what's inside of these, but they all will produce more layers and then you end up again with several activation maps, which are a number of channels if you put them together.
[01:03:05.760 → 01:03:15.760] So, you get another kind of shaped tensor in your convolutional neural network, which is then which you can pass this through following layers.
[01:03:15.760 → 01:03:30.760] So, it's at its core, CNN is essentially a sequence of convolutional layers and I haven't mentioned that it's each of these layers is of course followed by an activation function, otherwise everything will collapse into linearity and you could actually go back to have one single linear approximation.
[01:03:30.760 → 01:03:36.760] So, you're introduced in non-linearity by your activation functions in a sequence of convolutional layers.
[01:03:36.760 → 01:03:49.760] So, by the sequence important convolutional layers are responsible for feature extraction, so they take the raw data and produce a set of feature maps that highlight various aspects of the data and then later layers highlight feature or feature combinations.
[01:03:49.760 → 01:03:58.760] So, this is becoming very abstract, very difficult to interpret for humans, but the later the layers, the more kind of abstract your features will get.
[01:03:59.760 → 01:04:18.760] The activation functions very often are rectified linear unit, but which we talked in a couple of slides, the role of activation functions is really to introduce the non-linearity into the network without non-linearity the network would not be able to capture complex relationships and patterns in the data and everything actually would become equivalent to a linear transformation.
[01:04:19.760 → 01:04:31.760] So, as we move through the layers in the scene and the dimensions of the intermediate tensors often change, for example, you may start with an input image that has dimension height with some color challenge channels.
[01:04:31.760 → 01:04:39.760] And as we progress through the layers, the dimensions may reduce or expand dependent on the operations being actually performed.
[01:04:39.760 → 01:04:55.760] Remember, each layer in the network is learning to recognize increasingly complex features. Early layers might capture simple patterns like edges and consents on by deeper layers might recognize more abstract features, features like the shape of a nose or contour of a cat's ear or eyes.
[01:04:56.760 → 01:05:08.760] Now, each filter in the convolution layer is defined by its dimensions, which for our purpose here, I noted previously as i j and k.
[01:05:08.760 → 01:05:15.760] So, for a single filter, the total number of parameters is i multiplied by j multiplied by k.
[01:05:16.760 → 01:05:28.760] But we're not quite done yet. There's one more parameter, the bias term. So, adding the bias term, we have i times j times k plus one parameter for each filter in a convolutional layer.
[01:05:28.760 → 01:05:36.760] The bias term is crucial because it allows the filter to have some flexibility effectively shifting the activation function to better fit the data.
[01:05:37.760 → 01:05:47.760] So, to give you a sense of scale, imagine you have a simple three by three by three filter. That's 27 weight parameters plus one bias totaling 28 parameters for this filter.
[01:05:47.760 → 01:06:01.760] When you scale this to multiple filters and layers, the number of parameters can grow quite large. But remember, this is often still much fewer than what you find in a fully connected network for pretty much the same task.
[01:06:01.760 → 01:06:18.760] So, this is all you need to calculate the number of learned parameters. There are some sometimes exam questions in this direction. How many parameters do you need to learn in this CNN or how many operations do you need to perform this independence, of course, also on the input tensor, if you're syrup heading or not.
[01:06:18.760 → 01:06:30.760] But this is all you need to do to really figure out how, how computational complex your network is and if it's still feasible to probably run through your iPhones, newer processor.
[01:06:31.760 → 01:06:42.760] Now, one more thing, there is something known as a one by one convolution and at the first glance, it might may seem almost trivial. A single value is multiplied by a single value weight and a bias is added.
[01:06:42.760 → 01:06:59.760] But its application is far more from trivial. So, the main utility of a one by one convolution is to reduce the depth of our network. Think of it as a way to perform dimensionality reduction across C across the depth of the feature map or the input tensor or the feature tensor.
[01:06:59.760 → 01:07:09.760] But we have a high number of channels in our input volume or one by one convolution can effectively condense that information. It's essentially a dot product operation across the depth of the input volume.
[01:07:09.760 → 01:07:21.760] And as with regular convolution, this one by one convolution slide over all the locations in the input. So, in a nutshell, one by one convolution is allow the network to learn how to aggregate many channels into fewer channels.
[01:07:21.760 → 01:07:27.760] And thereby really reducing the computation complexity while maintaining all the important features.
[01:07:27.760 → 01:07:48.760] There is a neat trick that has a big impact on the efficiency and performance of convolutional networks. You can also think of them as tiny, small, fully connected networks that you learn for each of the channels, depth and decide how to actually combine all of the feature maps into another activation map.
[01:07:49.760 → 01:08:00.760] So, another thing to consider is how do we actually move our kernel across the tensors and their two important concepts, padding and strides.
[01:08:00.760 → 01:08:06.760] So, let's first talk about strides. Strides refer to the step size that the filter takes as it slides across the input image.
[01:08:06.760 → 01:08:12.760] A slide of one means the filter moves one pixel at a time, which is the most straightforward case. This is what we discussed at this point.
[01:08:12.760 → 01:08:21.760] And as you can see in the example image with a 7 by 7 input in a 3 by 3 filter stride of one gives us a 5 by 5 output.
[01:08:21.760 → 01:08:31.760] However, have a larger stride like two, if you move a little bit more, a larger stride like two would mean the filter jumps two pixels at a time.
[01:08:31.760 → 01:08:44.760] Now, our example, this reduces the output then to a 3 by 3 image. Strides are really mainly used to reduce the spatial dimensions of the output volume, which in turn reduces the amount of parameters and computations in network.
[01:08:44.760 → 01:08:56.760] But you're dismissing some of your input, right? So, you're skipping, say, with a stride of two every second input, you're only aggregating it from your kernel.
[01:08:56.760 → 01:09:06.760] So, padding is the technique we already discussed of adding extra pixels around the input image. In this example, we're using no padding, meaning we are not adding any pixels around our 7 by 7 input.
[01:09:06.760 → 01:09:14.760] The primary purpose of padding is to control the spatial size of the output volumes, mostly to preserve the spatial dimensions of the input volumes that they match.
[01:09:14.760 → 01:09:27.760] So, these two parameters stride and padding give us really fine grain control or the architecture of our scene and allow us to manage the computation load and those are the depth and how far we can go with the feature extraction and everything very effectively.
[01:09:27.760 → 01:09:37.760] So, this parameters, if you understand how to work, will really allow you to understand all the error messages you might get when you implement something like that and say the shape of this tensor doesn't match the shape of the other tensor and so on.
[01:09:37.760 → 01:09:49.760] This is really what it means how you need to really, really be on top of your tensor shapes when you're implementing your convolutional neural networks.
[01:09:49.760 → 01:09:58.760] Yeah, right. So, this is 7 by 7, which is also 5 by 5, stride 2, result in a 3 by 3.
[01:09:58.760 → 01:10:10.760] Now, let's talk a little bit more about the computational complexity if you want to figure out the visibility of computing network or maybe if it's still suitable to do predictions in real time, say on your smartphone.
[01:10:10.760 → 01:10:18.760] Let's assume this. Let's assume a 5 by 5 convolution with a single filter will involve 25 multiplied at operations, pay input element.
[01:10:18.760 → 01:10:31.760] If we instead use two consecutive 3 by 3 convolutions, the first 3 by 3 convolution requires 9 operations pay input and the second 3 by 3 convolution also requires 9 operations at the output of the first operation.
[01:10:31.760 → 01:10:41.760] Right. So, there's something interesting about that even though 9 plus 9 is 18, which is less than 25, the real computation savings can be even greater in practice.
[01:10:42.760 → 01:10:50.760] That's because the first 3 by 3 convolution generates a smaller output size compared to the original input and the second 3 by 3 convolution works on this reduced size output, right.
[01:10:50.760 → 01:10:59.760] This approach offers a way to increase computational efficiency and also introduces an additional nonlinearity by having two activation functions instead of only one.
[01:10:59.760 → 01:11:08.760] This can, this can help the model to capture more complex functions, but factorizing a 5 by 5 into 2, 3 by 3 convolution is just one example how you can optimize the architecture.
[01:11:09.760 → 01:11:18.760] It's just an approximation and you won't learn exactly the same thing, but you have a way to make smaller constant less operations here.
[01:11:18.760 → 01:11:23.760] So, this is the number of operations we have for a 5 by 5 filter operation.
[01:11:23.760 → 01:11:31.760] And if we factor them out into 3 by 3, so we can can actually do this less parameters to learn, but we can.
[01:11:31.760 → 01:11:39.760] We can split them up into 3 by 3 into approximation just 100, 36 K, 176 K operations, 136 K.
[01:11:39.760 → 01:11:48.760] So, at the end of the day, we end up with significantly less operations and factorized convolution.
[01:11:48.760 → 01:11:56.760] There's also a way if you're familiar with signal processing, you know that certain filters can be separated into 2 and by 1, 1 by n operations.
[01:11:57.760 → 01:12:01.760] So, like Gaussian filter can be separated, for example, you can also assume similar things here.
[01:12:01.760 → 01:12:19.760] So, instead of filtering factorizing 3 by 3 and 3 by 3, you could say, okay, I'm composed now 5 by 1 with 1 by 5 filter, 1 after each other and separate convolution is really also reduced to aim to reduce the computation burden of one big convolution.
[01:12:20.760 → 01:12:37.760] So, if you break things down like this to gain computational efficiency, like in this example here, you have only 5 parameters for the first convolution on 5 for the second, which means you end up with just 10 parameters you need to learn.
[01:12:37.760 → 01:12:43.760] This effectively reduces the computation cost, but, but also the number of learnable parameters.
[01:12:43.760 → 01:12:52.760] This is also just an approximation technique. The catch is that it works well only if the original 5 by 5 filter can be accurately approximated by 2 smaller filters.
[01:12:52.760 → 01:13:03.760] In some cases, this approximation might lead to a loss of information or really representational power, but in practice, the efficiency gain often outweigh drawbacks, it really depends on your application.
[01:13:03.760 → 01:13:11.760] So, separate convolution is author way to make your network more efficient, but at the cost of probably less representational power.
[01:13:11.760 → 01:13:18.760] It's really a trade-off that makes sense when computational resources are concerned.
[01:13:18.760 → 01:13:25.760] Now, another important concept to dimensionality reduction in CNNs is the concept of pooling.
[01:13:26.760 → 01:13:37.760] Now, pooling at its core serves two main purposes. It performs a kind of aggregation, and it also downshambers the image of each other.
[01:13:37.760 → 01:13:47.760] So, this is also a very often referred to as permutation invariant aggregation. Essentially, the order in which the pixels appear in the window doesn't really affect the outcome of the pooling operation.
[01:13:47.760 → 01:13:53.760] This is really what contributes to its property of local translation invariants or shift invariants.
[01:13:53.760 → 01:14:03.760] Really regardless of minor shifts, and we're not talking really huge shifts here, but regardless of minor shifts or deformations in the input image, the pooling operation really remains largely unaffected.
[01:14:05.760 → 01:14:13.760] The most commonly used method for pooling is max pooling. So, max pooling, the examiner block a grid of pixels and select the maximum value across them.
[01:14:14.760 → 01:14:24.760] Simply as that. We choose a block and say where is the maximum, and the maximum here is six, so we enter into the output six.
[01:14:24.760 → 01:14:28.760] Nothing more than that. This is just what pooling means.
[01:14:28.760 → 01:14:34.760] You could also do average pooling, mean pooling, but max pooling is really one of the most common ones.
[01:14:35.760 → 01:14:46.760] Really what it does is it reduces resolution, hierarchical features as we progress through the layers pooling helps with the network to concentrate on increasing the abstract features.
[01:14:46.760 → 01:14:54.760] And it is to some degree shift and deformation invariant pooling really contributes to the networks robustness against small shifts and deformation of the input.
[01:14:54.760 → 01:15:03.760] So, if we go through this example here, here the output is eight, and this block the output is five, and this block the output should be four, right.
[01:15:03.760 → 01:15:06.760] So, this is our output of our pooling.
[01:15:06.760 → 01:15:14.760] No magic here. It's really just used to get quicker down with your dimensions and still retain a lot of information.
[01:15:14.760 → 01:15:19.760] Pooling, important pooling is usually applied to each channel separately.
[01:15:19.760 → 01:15:27.760] So, you would pool each channel separately and not do something like a volumetric pooling, but you can of course free to implement something like that and also to
[01:15:27.760 → 01:15:35.760] depth reduction in that place. I would rather use one by one, a convolution to reduce depth, and then maybe if you need pooling to pull down

## Shift Invariance and Aliasing in CNNs

[01:15:36.760 → 01:15:40.760] to pool this to pool down each channel separately.
[01:15:40.760 → 01:15:47.760] So, while pooling is a prevalent approach to down sampling and gfing translation invariance, it's not really the only one, the only approach that can be used.
[01:15:47.760 → 01:15:57.760] You can just use bigger convolutions and to go down very quickly with your dimensionality as well, like strides pooling strides are probably quite similar in what they're doing.
[01:15:57.760 → 01:16:05.760] Here I have another example of what aque variance and invariance in practical networks look like.
[01:16:05.760 → 01:16:15.760] So, this is an example from the MNIST database. There's something many of you might have heard already about, but this is a kind of simple toy dataset that is often used to play around with neural networks and CNNs and so on.
[01:16:15.760 → 01:16:27.760] It's handwritten numbers that are different and scanned from the 90s. We'll talk about them a little bit more, but this is just one of these digits.
[01:16:27.760 → 01:16:38.760] So, if I convolve this image with this filter kernel, then the output looks like this and the output of this convolution layer is to some degree shifting variance.
[01:16:38.760 → 01:16:53.760] If I move this a little bit, then you see that the kernel, so the colors here and the features that are extracted don't really change, but the output changes equally to how the input changes.
[01:16:53.760 → 01:17:07.760] Now, pooling introduces some shift invariance. So, if I have this pooling layer here on top of this feature, I'm appeared and say the maximum is this red pixel here, then when I just store this one in the next layer,
[01:17:07.760 → 01:17:19.760] and if I move this a little bit around, then the maximum will still be the same. Well, within the limits of how far I pool, of course, but this maximum will still be invariant.
[01:17:19.760 → 01:17:28.760] And so, the output will all be the same. No matter how much I shift the input within reason within that pooling perceptive field.
[01:17:29.760 → 01:17:35.760] Now, it's not quite true, of course, there's a lot to it. I would recommend you to have a look at this paper.
[01:17:35.760 → 01:17:44.760] The cruiser thing is to understand is that we are violating heavily what we know from signal processing about how to sample a function to approximate it.
[01:17:44.760 → 01:17:52.760] This is known as the Nygris sampling theory. And the Nygris sampling theory really states that a continuous signal can be completely represented by its samples.
[01:17:53.760 → 01:17:58.760] And fully reconstructed, if it's sampled at least twice as far as the highest frequency component.
[01:17:58.760 → 01:18:02.760] And that's something we can clearly violate with however we set up pooling in these things.
[01:18:02.760 → 01:18:09.760] And so, traditional pooling methods, including max pooling and stride at convolutions, they often don't adhere simply to this theorem.
[01:18:09.760 → 01:18:20.760] So, they perform what is known as aliasing, which can really cause a loss of information and make the CNN sensitive to small shifts and translations in the inputs that are dependent on how you implement this.
[01:18:20.760 → 01:18:27.760] Small shifts will really dramatically sometimes impact the output of your network, which is not really what you want to achieve.
[01:18:27.760 → 01:18:38.760] So, this paper by Tsang here shows that when integrated correctly, anti-alizing techniques can coexist with traditional down sampling methods like max pooling and stride at convolutions.
[01:18:38.760 → 01:18:48.760] This not only improves the model's accuracy and benchmarks like the image net dataset, but does enhances its generalization capabilities and making it more stable.
[01:18:49.760 → 01:18:59.760] All of these sort of directions exist that we recommend you to sometimes have a look in these papers where a lot of these aspects are considered in detail.

## Rotation Equivariance and Harmonic Networks

[01:18:59.760 → 01:19:06.760] So, if it's not clear, if it's not completely clear what I mean by frequencies in images, please have a look at the videos.
[01:19:06.760 → 01:19:11.760] I have linked here as there's a medium article what's meant by frequencies.
[01:19:11.760 → 01:19:19.760] So, these are really just the gradient components, the edge components and the rate of change basically from pixel to pixel is already discrete.
[01:19:19.760 → 01:19:21.760] So, it's already straight forward to understand.
[01:19:21.760 → 01:19:24.760] Please have a look at this material if you're not sure what this means here.
[01:19:24.760 → 01:19:27.760] Frequency in audio and other kind of signals.
[01:19:27.760 → 01:19:31.760] I assume most of you had some signal processing in the past.
[01:19:31.760 → 01:19:33.760] So, this should be quite clear.
[01:19:34.760 → 01:19:47.760] So, another example from the paper from a sound before is really that max pooling can break shift aque variance to some degrees of the assumptions we make are very, very weak sometimes.
[01:19:47.760 → 01:19:54.760] So, if you're seeing this input signal here, which just goes up and down and up and down and we sample it a couple of times.
[01:19:54.760 → 01:19:58.760] And then max pool the things, so we say let's do a maximum between those.
[01:19:58.760 → 01:20:02.760] So, this is 0 and a maximum between those.
[01:20:02.760 → 01:20:05.760] This is 1.
[01:20:07.760 → 01:20:11.760] And then a maximum here, which is again 0 and a maximum of 1.
[01:20:11.760 → 01:20:16.760] Well, okay, this is a sort of reasonable approximation of that 0, 1, 0, 1 function.
[01:20:16.760 → 01:20:19.760] That's okay, right? But I was just lucky.
[01:20:19.760 → 01:20:24.760] The reason is because I chose to max between those between those between those.
[01:20:24.760 → 01:20:32.760] And what if I would shift that max pool windows slightly and I would build now the max between these four and then another four and another four.
[01:20:32.760 → 01:20:37.760] Actually, what I would end up with is complete a live sing and I under sample my function here.
[01:20:37.760 → 01:20:48.760] And suddenly I get a function that's consistently one constantly one, which is definitely not a good approximation of this changing 0, 1, 0, 1, 0, 1 function.
[01:20:48.760 → 01:20:55.760] So there's a big difference and the only the only change I made is a small shift in how I sample with my max pooling function.
[01:20:55.760 → 01:21:03.760] The solution is informed by classic computer vision if you don't sample and utilizing, anti-alizing techniques, then some of these effects go away.
[01:21:03.760 → 01:21:16.760] But do not expect that CNNs without careful design really adhere to everything we know from how to represent signals sufficiently well from signal theory.
[01:21:16.760 → 01:21:23.760] So there's also quite interesting observations beyond shifts and equipariances.
[01:21:23.760 → 01:21:42.760] So if you consider them to be group operations, so say rotations, for example, then we can explore things like harmonic nets, harmonic nets have been explored in the past already where specific focus has been put on rotation equipariance.
[01:21:42.760 → 01:21:47.760] So as you can imagine rotation works a little bit differently in the group than translation alone.
[01:21:47.760 → 01:21:54.760] So you can also watch this video translation every variance.
[01:21:54.760 → 01:21:59.760] If we translate an input image to a CNN, then this feature will also translate with a proportional step size.
[01:21:59.760 → 01:22:07.760] Importantly, if we place a motion compensated window around the features, then we see that the form of a translated features remains stable, independent of translation.
[01:22:07.760 → 01:22:13.760] This property arises by design from the translational weightis structure of CNNs.
[01:22:13.760 → 01:22:18.760] If instead we consider input rotations, then we see that the motion compensated features are not stable at all.
[01:22:18.760 → 01:22:25.760] Moreover, it is non-trivial to infer the relationship between two feature maps which should differ only in rotate.
[01:22:25.760 → 01:22:28.760] Right, so this is the problem that we want to solve.
[01:22:28.760 → 01:22:32.760] Translation equipariance. If we translate an input image to a CNN.
[01:22:32.760 → 01:22:40.760] Let's go to the solution. So if you do this with proper harmonics and explore the invariance within the internet, then...
[01:22:40.760 → 01:22:46.760] Returning to our motion compensated view of the feature maps, we see that indeed harmonic networks are able to preserve deep features on rotation.
[01:22:46.760 → 01:22:51.760] Viewing the two motion compensated windows side by side, the extra stability we are granted becomes...
[01:22:51.760 → 01:22:57.760] So you see the features just become more stable regardless of how the input is rotated.
[01:22:57.760 → 01:23:06.760] You don't need to learn all of these architectures, but how they are just interesting side steps to have recommendations for reading papers or watching a video.

## Deformations and Generalization in CNNs

[01:23:14.760 → 01:23:23.760] There is probably no golden path yet to one size fits all approach. It really depends on the sort of problem you are dealing with.
[01:23:23.760 → 01:23:30.760] So far we have only talked about feature extractors. And that is fine.
[01:23:30.760 → 01:23:43.760] So if we look at what they try to achieve in terms of echari variance, our goal is to appreciate the extent to which a CNN can be adopted to capture a variety of transformations.
[01:23:43.760 → 01:23:49.760] What's about deformations? Imagine a canonical representation of the number three here from the MNES dataset.
[01:23:49.760 → 01:23:57.760] Now consider all the possible ways people might write this number. Some might elongate the curve, others might write it more compressed.
[01:23:57.760 → 01:24:04.760] But these are what we termed deformations. I can deform each of these strokes slightly differently.
[01:24:04.760 → 01:24:09.760] And they can be represented mathematically as warping operations like in a vector field.
[01:24:09.760 → 01:24:15.760] In a more technical sense of warping operation applies a smooth deformation field to the pixels of an image.
[01:24:15.760 → 01:24:21.760] If you shifting them slightly to create really a new yet similar arrangement, right?
[01:24:21.760 → 01:24:27.760] Still semantically the same, but it's completely new arrangement. It's not just translated or rotated, it's really deform to a vector field.
[01:24:27.760 → 01:24:32.760] And it's this small localized shifts that CNNs are particularly good at handling.
[01:24:32.760 → 01:24:40.760] This explains why CNNs excel really at tasks like handwriting, digit classification, cats and dogs and all sorts of image classification tasks.
[01:24:40.760 → 01:24:54.760] So CNNs are not just invariant to large scale, easily defined transformations like shifts and rotations, but they're particularly good in approximating and approximately invariant to this really subtle and small complex deformations.
[01:24:54.760 → 01:25:04.760] And this adaptability really makes CNNs quite powerful tool for why the ray of image recognition task from medical imaging to autonomous driving.
[01:25:04.760 → 01:25:17.760] While CNNs were originally designed with translation invariants in mind, the utility really extends far beyond allowing them to capture and generalize well, even in the presence of really, really complex deformations and variants of the features.
[01:25:17.760 → 01:25:23.760] This is what we call generalize ability.

## Flattening Layers in CNNs

[01:25:23.760 → 01:25:35.760] So flattening layers is the last thing we'll discuss for the basic building blocks of a scene and flattening layers just surface connection between convolutional layers and fully connected layers in a convolutional network.
[01:25:35.760 → 01:25:49.760] You might still want to have the final kind of lower dimensional space because if I fully connected network, so for that, you need, you need to flatten your input. And this is exactly what the flattening layer does.
[01:25:49.760 → 01:26:05.760] So the convolutional pooling layers usually operate on a 3d tensor that or higher dimensional tensor that represents the learned features from the input image fully connected layer expects a 1d tensor of numbers, a 1d vector large one, but a 1d vector.
[01:26:05.760 → 01:26:21.760] And the flattening layer just reshapes 3d or nd tensors into 1d tensors. So let's say the output of your final pooling layer in a CNN is a tensor of shape 4x4x64, which means it has height of 4 and with 4 and the depth of 64 features.
[01:26:21.760 → 01:26:30.760] The flattening layer will take this 4x4 by 64 tensor and reshape it in a 1d tensor of shape 1x1024 without ordering the actual data.
[01:26:30.760 → 01:26:38.760] And the flattening data then can serve as an input to a fully connected layer, also known as dense layers, because here now everything is connected to everything.
[01:26:38.760 → 01:26:55.760] And also locality is our two principles, invariance and locality, they don't play such a big role in such abstract space anymore. So you can just connect everything to everything again and make a decision dependent on every single of these abstract inputs.
[01:26:56.760 → 01:27:07.760] So the flattening layer doesn't learn any parameters, it only reform reformers the data. But this step is very often crucial for transforming the spatial features into a format that can be fed into standard fully connected layers.
[01:27:08.760 → 01:27:22.760] A lot of the networks will discuss examples where action network architectures will use this sort of principle and have in the final bit fully connected layer to make the final code what class, your object actually is.
[01:27:22.760 → 01:27:38.760] Now what do CNNs actually learn to have a look at some feature outputs of very early layers, some of these look like with a feature extractors, but not really clear what the features, this seems to be corners, this seems to be round these things.

## Interpretability of CNN Features

[01:27:22.760 → 01:27:38.760] Now what do CNNs actually learn to have a look at some feature outputs of very early layers, some of these look like with a feature extractors, but not really clear what the features, this seems to be corners, this seems to be round these things.
[01:27:38.760 → 01:28:01.760] If you go a little bit further down layer three seems to become more abstract like shapes of paper and like these patterns here, then if you go higher up layer four, this seems to react to things that look like legs and eyes and this is what people try to analyze in the networks, but very often these will just not be interpretable.
[01:28:01.760 → 01:28:27.760] The features beyond the certain layer are not kind of straightforward to interpret as you would expect like no is eyes and sometimes you see them very often you don't, but the performance of your classifier is still excellent, which led you live with a certain black box characteristics of these networks explainable machine learning is still a big topic here, but it's because it's difficult to comprehend for us human.
[01:28:27.760 → 01:28:42.760] How these high-dimensional projections work don't get me wrong with fully understand what's happening, these are not the magic machines, we exactly know what's happening, it's just very difficult for us to follow the many, many high-dimensional decision paths, a combination of features thus.
[01:28:42.760 → 01:28:49.760] So that is an active area of research, how can we make explanations of networks that are comprehensive for humans.
[01:28:50.760 → 01:29:00.760] And text is probably a solution, but also certain other kind of feature combinations, which you can talk about in a couple of slides.
[01:29:00.760 → 01:29:18.760] Historically, a lot of people say a lot of this has been invented a long, long time ago, maybe the basic overview is that the neocognitron has been explored by Fukushima in the 70s 80s.

## Historical Context of CNNs

[01:29:00.760 → 01:29:18.760] Historically, a lot of people say a lot of this has been invented a long, long time ago, maybe the basic overview is that the neocognitron has been explored by Fukushima in the 70s 80s.
[01:29:18.760 → 01:29:27.760] He didn't have a way to learn them, so this didn't have back propagation yet to learn the way so they were kind of handcrafted, but already had the idea of
[01:29:27.760 → 01:29:36.760] bat sharing and locality and so on and Fukushima X-ray also used something like Arrelu, non linearity, which we'll talk about in the future lecture.
[01:29:37.760 → 01:29:52.760] LeCun did a lot of work here in the direction of the conversion networks, image analysis in the 90s, but the program was always there was no super efficient tens of hardware like GPUs around that make really large-scale programs so
[01:29:52.760 → 01:30:07.760] the data LeCun was working with were very, very small kind of 28x28 pixel images that he classified and then really as soon as GPUs became flexible enough to be programmed, the one of the kind of hardest challenges in computer vision was
[01:30:07.760 → 01:30:23.760] very good performance, let's put it that way, very good performance on one of these challenges was shown and then the rise of the learning and GPUs really came in 2012, but it depends how you measure these things.
[01:30:23.760 → 01:30:40.760] So GPUs had a big factor, but also a lot of the components have been around for quite a while, and since we have most of these things now together there's a lot of discoveries to make actually about data and statistics and high-dimensional spaces and all of these things, this is very, very exciting.
[01:30:41.760 → 01:30:57.760] So I've prepared also in the lecture notes some of these summary slides, so have a look of them, don't learn them by heart please, I'm really the most important part is that you understand and develops an intuition what's actually going on here.
[01:30:57.760 → 01:31:14.760] The practical implementation you're learning during the coursework where you see most of this is using pytorch in the right way, but then understanding the individual steps, this is what you learn here in the lectures and interpreting the arrows you'll encounter when you build up your network.
[01:31:14.760 → 01:31:43.760] And then what I also hope that you take out of it is to understand how these links to master probability theory and so on where well paper is thinking for a long time since base actually about what it means, how likely a prediction can be made, given a certain input, like a combination of features, so this can be very well described or with probability theory in this context, we'll talk a little bit more about this in the context of later lectures.

