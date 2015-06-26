# Aim of this project

We often faced cases where some tasks need to be done in background, but although some of them can be short enough to not notice them, others are long enough to at least let the user know that something is running. This project provides simple and user-friendly features to account for such progress. In particular, it provides what is needed to easily store and retrieve the progress of a task, but also facilities to display it on the standard output (or any other textual stream) or in a graphical way.

# How to use it?

## Add it to your project

It is available on [Maven](http://search.maven.org/#search|ga|1|a%3A%22progress-core%22%20g%3A%22fr.matthieu-vergne%22), so you can get the JAR or directly add it to your POM (check the [available releases](https://github.com/matthieu-vergne/Progress/releases) to know the last version):
```
<dependency>
    <groupId>fr.matthieu-vergne</groupId>
    <artifactId>progress-core</artifactId>
    <version>1.0</version>
</dependency>
```

## Manage a single task

Several samples are provided in `progress-sample`, but one can basically create a `Progress` instance:
```
// Creation of a progress going from 0 to 10
ManualProgress<Integer> progress = new ManualProgress<Integer>(0, 10);
```
update it when required:
```
progress.setCurrentValue(5);
```
read it when required:
```
// How many steps have been done?
progress.getCurrentValue();

// How many steps are needed before to finish?
progress.getMaxValue();

// Is it finished?
progress.isFinished();
```

## Combine several tasks into a global one

The `ProgressFactory` provides a simple way to combine several `Progress` instances into a single one:
```
// Create a progress for each sub-task
Collection<Progress<Integer>> subProgresses = ...;

// Combine them into a global one
Progress<Integer> global = factory.createCombinedProgress(subProgresses);
```

The global `Progress` can be used like any other ones, so you can get its current value, max value and finished state, among other things. You can look at a [sample using a combined `Progress`](https://github.com/matthieu-vergne/Progress/blob/master/progress-samples/src/main/java/fr/vergne/progress/sample/CombinedSample.java) for a more complete example.

## Display a progress

Usually, one would like to manage the progress of a task in order to display its status at some convenient time. While it is easy to just get the relevant values from the progress and display them where required, we have a `ProgressUtil` utilitary class which provides some facilities.

Among others, you can display a `Progress` on the command line each time it is updated:
```
ProgressUtil.displayProgressOnOutputStream(progress, System.out);
```
or display it on a regular basis:
```
// Display every second
ProgressUtil.displayProgressOnOutputStream(progress, System.out, 1000, false);
```
You can also display it in a JDialog for graphical display:
```
ProgressUtil.displayProgressOnDialog(progress, false);
```

## And more...

Other facilities are supported, like:
- set the maximum value only when it is known,
- register a `ProgressListener` to be informed when a `Progress` is updated,
- build a `JProgressBar` to manually insert it into an existing Swing GUI,
- etc.

Of course, feedbacks are welcome! {^_°}