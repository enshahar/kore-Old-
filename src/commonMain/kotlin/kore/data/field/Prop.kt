package kore.data.field

import kore.data.Data
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty

typealias Prop<T> = PropertyDelegateProvider<Data, ReadWriteProperty<Data, T>>