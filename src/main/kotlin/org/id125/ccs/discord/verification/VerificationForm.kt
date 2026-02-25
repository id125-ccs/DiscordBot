package org.id125.ccs.discord.verification

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.centauri07.promptlin.core.form.FormSessionScope
import me.centauri07.promptlin.core.form.form
import me.centauri07.promptlin.core.prompt.input.handlers.StringInputHandler
import me.centauri07.promptlin.discord.prompt.choice.ButtonOption
import me.centauri07.promptlin.discord.prompt.choice.SelectOption
import org.id125.ccs.discord.AppContext
import org.id125.ccs.discord.profile.Campus
import org.id125.ccs.discord.profile.College

val verificationForm = form {
    val consent = choice<ButtonOption>(
        "consent", "Privacy Consent",
        """
            By continuing, you acknowledge and consent to the collection, storage, and use of your personal data for the purpose of account verification and maintaining a secure and trusted community for DLSU CCS students. 

            Only non-sensitive information, such as your display name and general profile details, may be visible to other users through features like /profile. Sensitive information, including your DLSU email and internal identifiers, will remain private and will never be shared publicly.

            You may withdraw your consent at any time by unverifying your account through the designated process (to be made available soon). Upon unverification, all personal data associated with your account will be permanently deleted, and your profile will no longer be visible to other users.
            
            __**| IMPORTANT |**__ Should you wish to cancel this verification process, type `ccs!fcancel`
        """.trimIndent()
    ) {
        option(ButtonOption("agree", "I Agree", "", style = 3))
        option(ButtonOption("disagree", "I Do Not Agree", "", style = 4))
    }

    val consentGranted: FormSessionScope.() -> Boolean = { get(consent).value == "agree" }

    val university = choice<ButtonOption>("university", "University", "Which university are you from?") {
        includeIf { consentGranted() }
        listOf(
            ButtonOption("dlsu", "De La Salle University", "", style = 3),
            ButtonOption("other", "Other University", "")
        ).forEach(::option)
    }

    val isDlsu: FormSessionScope.() -> Boolean = { consentGranted() && get(university).value == "dlsu" }

    val batchId = input(
        StringInputHandler,
        "batch_id",
        "Batch ID",
        "Enter your batch ID (first three digits of your ID Number)."
    ) {
        includeIf { consentGranted() && isDlsu() }
        validate("Invalid batch ID number.") {
            it.length == 3 && it.toIntOrNull()?.let { parsed -> parsed in 99..125 } == true
        }
    }

    val campus = choice<ButtonOption>("campus", "Campus", "Which DLSU campus are you from?") {
        includeIf { consentGranted() && isDlsu() }

        Campus.entries.forEach {
            option(ButtonOption(it.name, it.displayName, "", it.emoji, style = 3))
        }
    }

    val college = choice<SelectOption>("college", "College", "Which college are you enrolled in?") {
        includeIf { consentGranted() && isDlsu() }

        College.entries.forEach {
            option(SelectOption(it.abbreviation.lowercase(), it.abbreviation, it.displayName, it.emoji))
        }
    }

    val degreeProgram = input(StringInputHandler,"degree_program", "Degree Program Code", "What is your degree program code?") {
        includeIf { consentGranted() && isDlsu() }
        validate("Maximum length exceeded for degree program code.") { it.length <= 7 }
    }

    input(StringInputHandler, "name", "Name", "Enter your preferred display name.") {
        includeIf { consentGranted() && isDlsu() }
        validate("Display name exceeded the maximum for preferred display name.") {
            val suffix = " | [${get(degreeProgram)}] [${get(batchId)}]"
            val maxBaseLength = 32 - suffix.length

            it.length <= maxBaseLength
        }
    }

    val email = input(
        StringInputHandler, "email", "DLSU Email", "**You’re almost set!** To complete your registration, " +
                "please enter your __**official DLSU email address**__. This will allow us to verify that you are a student at " +
                "De La Salle University and ensure that your account is securely linked to your university identity. Once submitted," +
                " a verification code will be sent to this email, which you will need to enter to finalize your setup"
    ) {
        includeIf { consentGranted() && isDlsu() }

        validate("Invalid DLSU email format.") { email ->
            email.matches(Regex("^[A-Za-z0-9._-]+@dlsu\\.edu\\.ph$"))
        }

        validate("Email already registered.") { email ->
            !VerificationService.isEmailRegistered(email)
        }

        onComplete { email ->
            AppContext.coroutineScope.launch(Dispatchers.IO) {
                VerificationService.sendVerificationCode(email)
            }
        }
    }

    val code = input(
        StringInputHandler,
        "code",
        "Email Code",
        "Please enter the __**6‑digit**__ verification code sent to your official DLSU email address."
    ) {
        includeIf { consentGranted() && isDlsu() }
        validate("Incorrect code.") { VerificationService.verify(get(email), it) }
    }
}