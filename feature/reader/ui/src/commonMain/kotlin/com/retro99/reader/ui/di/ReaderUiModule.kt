package com.retro99.reader.ui.di

import nl.adaptivity.xmlutil.serialization.XML
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(
    includes = [
        PlatformReaderModule::class,
    ],
)
@ComponentScan("com.retro99.reader.ui")
class ReaderUiModule {

    @Single
    fun provideXml(): XML = XML.v1 {
        // Be lenient with unknown attributes and elements (like version, xmlns:epub, etc.)
        policy {
            ignoreUnknownChildren()
        }
    }
}

