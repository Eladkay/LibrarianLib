package com.teamwizardry.librarianlib.core

import com.teamwizardry.librarianlib.LibrarianLibModule
import org.apache.logging.log4j.LogManager

public object LibrarianLibCoreModule : LibrarianLibModule("core", "Core")

internal val logger = LibrarianLibCoreModule.makeLogger(null)
