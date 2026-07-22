package com.diamon.calculo.terminal;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Linux-style terminal view with matrix green text on deep black background.
 * Supports command input, output display, and command history.
 */
public class LinuxTerminalView extends LinearLayout {

    private static final int COLOR_BG = Color.parseColor("#0A0A0A");
    private static final int COLOR_GREEN = Color.parseColor("#00FF00");
    private static final int COLOR_ERROR = Color.parseColor("#FF4444");
    private static final int COLOR_SYSTEM = Color.parseColor("#FFD700");
    private static final int COLOR_PROMPT = Color.parseColor("#00CC00");
    private static final String PROMPT = "ops@opensees:~$ ";

    private ScrollView scrollView;
    private TextView tvOutput;
    private EditText etInput;
    private Handler mainHandler;

    // Command history
    private List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;
    private static final int MAX_HISTORY = 50;

    // Listener
    private OnCommandListener commandListener;

    // Blinking cursor animation
    private boolean cursorVisible = true;
    private Runnable cursorBlink;

    public interface OnCommandListener {
        void onCommand(String command);
    }

    public LinuxTerminalView(Context context) {
        super(context);
        init(context);
    }

    public LinuxTerminalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mainHandler = new Handler(Looper.getMainLooper());
        setOrientation(VERTICAL);
        setBackgroundColor(COLOR_BG);
        setPadding(8, 8, 8, 8);

        // Output area
        scrollView = new ScrollView(context);
        scrollView.setBackgroundColor(COLOR_BG);
        scrollView.setFillViewport(true);

        tvOutput = new TextView(context);
        tvOutput.setTextColor(COLOR_GREEN);
        tvOutput.setTypeface(Typeface.MONOSPACE);
        tvOutput.setTextSize(13f);
        tvOutput.setBackgroundColor(COLOR_BG);
        tvOutput.setTextIsSelectable(true);
        tvOutput.setPadding(8, 8, 8, 8);
        tvOutput.setGravity(Gravity.TOP | Gravity.START);

        scrollView.addView(tvOutput, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        addView(scrollView, new LayoutParams(
                LayoutParams.MATCH_PARENT, 0, 1.0f));

        // Input area
        LinearLayout inputRow = new LinearLayout(context);
        inputRow.setOrientation(HORIZONTAL);
        inputRow.setBackgroundColor(Color.parseColor("#111111"));
        inputRow.setPadding(4, 4, 4, 4);

        // Prompt label
        TextView tvPrompt = new TextView(context);
        tvPrompt.setText(PROMPT);
        tvPrompt.setTextColor(COLOR_PROMPT);
        tvPrompt.setTypeface(Typeface.MONOSPACE);
        tvPrompt.setTextSize(13f);
        tvPrompt.setPadding(8, 12, 0, 12);
        inputRow.addView(tvPrompt, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Input field
        etInput = new EditText(context);
        etInput.setTextColor(COLOR_GREEN);
        etInput.setHintTextColor(Color.parseColor("#336633"));
        etInput.setHint("Enter command...");
        etInput.setTypeface(Typeface.MONOSPACE);
        etInput.setTextSize(13f);
        etInput.setBackgroundColor(Color.TRANSPARENT);
        etInput.setSingleLine(true);
        etInput.setImeOptions(EditorInfo.IME_ACTION_SEND);
        etInput.setPadding(4, 8, 8, 8);

        etInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                submitCommand();
                return true;
            }
            return false;
        });

        inputRow.addView(etInput, new LayoutParams(
                0, LayoutParams.WRAP_CONTENT, 1.0f));

        addView(inputRow, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Welcome banner
        showWelcomeBanner();
    }

    private void showWelcomeBanner() {
        String banner =
                "╔══════════════════════════════════════════════════╗\n" +
                "║  Structural & Seismic Research - OpenSees v3.8  ║\n" +
                "║  Powered by OpenSees (UC Berkeley)              ║\n" +
                "║  Type 'help' for available commands             ║\n" +
                "╚══════════════════════════════════════════════════╝\n\n";
        appendSystem(banner);
    }

    private void submitCommand() {
        String cmd = etInput.getText().toString().trim();
        if (cmd.isEmpty()) return;

        // Add to history
        commandHistory.add(cmd);
        if (commandHistory.size() > MAX_HISTORY) {
            commandHistory.remove(0);
        }
        historyIndex = commandHistory.size();

        // Show command in output
        appendOutput(PROMPT + cmd);

        // Clear input
        etInput.setText("");

        // Notify listener
        if (commandListener != null) {
            commandListener.onCommand(cmd);
        }
        
        etInput.postDelayed(() -> {
            etInput.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    // ==================== Public API ====================

    public void setCommandListener(OnCommandListener listener) {
        this.commandListener = listener;
    }

    public void appendOutput(String text) {
        mainHandler.post(() -> {
            tvOutput.append(text + "\n");
            scrollToBottom();
        });
    }

    public void appendError(String text) {
        mainHandler.post(() -> {
            SpannableString span = new SpannableString(text + "\n");
            span.setSpan(new ForegroundColorSpan(COLOR_ERROR), 0, span.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvOutput.append(span);
            scrollToBottom();
        });
    }

    public void appendSystem(String text) {
        mainHandler.post(() -> {
            SpannableString span = new SpannableString(text + "\n");
            span.setSpan(new ForegroundColorSpan(COLOR_SYSTEM), 0, span.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvOutput.append(span);
            scrollToBottom();
        });
    }

    public void clearOutput() {
        mainHandler.post(() -> {
            tvOutput.setText("");
            showWelcomeBanner();
        });
    }

    private void scrollToBottom() {
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
