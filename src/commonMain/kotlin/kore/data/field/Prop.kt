package kore.data.field

import kore.data.VO
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty

typealias Prop<T> = PropertyDelegateProvider<VO, ReadWriteProperty<VO, T>>