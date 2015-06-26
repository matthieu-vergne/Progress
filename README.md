# Aim of this project

We often faced cases where some tasks need to be done in background, but although some of them can be short enough to not notice them, others are long enough to at least let the user know that something is running. This project provides simple and user-friendly features to account for such progress. In particular, it provides what is needed to easily store and retrieve the progress of a task, but also facilities to display it on the standard output (or any other textual stream) or in a graphical way.

# How to use it?

It is available on Maven, so you can get the JAR or directly add it to your POM (check the [available releases](https://github.com/matthieu-vergne/Progress/releases) to know the last version):
```
<dependency>
    <groupId>fr.matthieu-vergne</groupId>
    <artifactId>progress-core</artifactId>
    <version>1.0</version>
</dependency>
```

Several samples are provided in `progress-sample`, but one can basically create a `Progress` instance:
```
// Object used to update the progress instance
ProgressSetter<Integer> setter = new ProgressSetter<Integer>();

// Creation of a progress going from 0 to 10
ProgressFactory factory = new ProgressFactory();
Progress<Integer> progress = factory.createManualProgress(setter, 0, 10);
```
update it when required:
```
setter.setCurrentValue(5);
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
display it on the command line:
```
// Display each time it is updated
ProgressUtil.displayProgressOnOutputStream(progress, System.out);

// Display every second
ProgressUtil.displayProgressOnOutputStream(progress, System.out, 1000, false);
```
or in a JDialog for graphical display:
```
ProgressUtil.displayProgressOnDialog(progress, false);
```

Other facilities are supported, like the ability to set the maximum value only when it is known, register `ProgressListener` instances to be informed when the `Progress` is updated, etc.