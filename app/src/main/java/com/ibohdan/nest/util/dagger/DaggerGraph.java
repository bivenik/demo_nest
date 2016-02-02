package com.ibohdan.nest.util.dagger;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {DefaultModule.class})
public interface DaggerGraph extends AbsGraph {
}
