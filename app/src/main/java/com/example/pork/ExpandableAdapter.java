package com.example.pork;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ExpandableAdapter extends RecyclerView.Adapter<ExpandableAdapter.ViewHolder> {
    private List<ExpandableItem> itemList;

    public ExpandableAdapter(List<ExpandableItem> itemList) {
        this.itemList = itemList;
    }

    // 合并后的 ViewHolder 类
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        TextView recipeTitleTextView;
        TextView ingredientsTextView;
        TextView recipeContentTextView;
        TextView dateTextView;
        ImageView imageView;
        TextView expandToggle;
        View expandableLayout;
        LinearLayout questionnaireLayout;

        RadioGroup question1;
        RadioGroup question2;
        RadioGroup question3;
        RadioGroup question4;
        EditText openQuestion;
        Button submitButton;

//        TextView questionTextView;
//        Button yesButton;
//        Button noButton;
        TextView feedbackTextView;

        ViewHolder(View itemView) {
            super(itemView);
            // 初始化所有视图元素
            titleTextView = itemView.findViewById(R.id.idView);
            contentTextView = itemView.findViewById(R.id.valueView);
            recipeTitleTextView = itemView.findViewById(R.id.recipeTitleView);
            ingredientsTextView = itemView.findViewById(R.id.ingredientsView);
            recipeContentTextView = itemView.findViewById(R.id.recipeContentView);
            dateTextView = itemView.findViewById(R.id.dateView);
            imageView = itemView.findViewById(R.id.resultImageView);
            expandToggle = itemView.findViewById(R.id.expandToggle);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            questionnaireLayout = itemView.findViewById(R.id.questionnaireLayout);

            question1 = itemView.findViewById(R.id.question1);
            question2 = itemView.findViewById(R.id.question2);
            question3 = itemView.findViewById(R.id.question3);
            question4 = itemView.findViewById(R.id.question4);
            openQuestion = itemView.findViewById(R.id.openQuestion);
            submitButton = itemView.findViewById(R.id.submitButton);
            feedbackTextView = itemView.findViewById(R.id.feedbackTextView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expandable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpandableItem item = itemList.get(position);

        // 绑定数据到视图
        holder.titleTextView.setText(item.getTitle());
        holder.contentTextView.setText(item.getContent());
        holder.recipeTitleTextView.setText(item.getRecipeTitle());
        holder.ingredientsTextView.setText(item.getIngredients());
        holder.recipeContentTextView.setText(item.getRecipeContent());
        holder.dateTextView.setText(item.getDate());

        Glide.with(holder.imageView.getContext()).load(item.getImageUrl()).into(holder.imageView);

        holder.expandToggle.setOnClickListener(v -> {
            boolean isExpanded = holder.expandableLayout.getVisibility() == View.VISIBLE;

            if (isExpanded) {
                // 收起問卷與食譜
                holder.expandableLayout.setVisibility(View.GONE);
                holder.expandToggle.setText("查看食譜");
            } else {
                // 展开食谱和问卷部分
                holder.expandableLayout.setVisibility(View.VISIBLE);
                holder.expandToggle.setText("收起食譜");
            }
        });

        holder.submitButton.setOnClickListener(v -> {
            if (validateAnswers(holder)) {
                String answer1 = getSelectedAnswer(holder.question1);
                String answer2 = getSelectedAnswer(holder.question2);
                String answer3 = getSelectedAnswer(holder.question3);
                String answer4 = getSelectedAnswer(holder.question4);
                String openAnswer = holder.openQuestion.getText().toString();

                // 在这里处理答案，例如将数据发送到服务器或保存到本地数据库

                holder.feedbackTextView.setText("感謝您的反饋!");
                updateViewAfterFeedback(holder);

                holder.questionnaireLayout.setVisibility(View.GONE);
            } else {
                Toast.makeText(v.getContext(), "請填寫選擇題。", Toast.LENGTH_SHORT).show();
            }
        });

        // 确保初始状态正确
        holder.feedbackTextView.setVisibility(View.GONE);
//        holder.questionTextView.setVisibility(View.VISIBLE);
//        holder.yesButton.setVisibility(View.VISIBLE);
//        holder.noButton.setVisibility(View.VISIBLE);
    }

    // 更新视图状态以避免重复按下按钮
    private void updateViewAfterFeedback(ViewHolder holder) {
//        holder.questionTextView.setVisibility(View.GONE);
//        holder.yesButton.setVisibility(View.GONE);
//        holder.noButton.setVisibility(View.GONE);

        holder.feedbackTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // 验证答案是否都已选择
    private boolean validateAnswers(ViewHolder holder) {
        return holder.question1.getCheckedRadioButtonId() != -1 &&
                holder.question2.getCheckedRadioButtonId() != -1 &&
                holder.question3.getCheckedRadioButtonId() != -1 &&
                holder.question4.getCheckedRadioButtonId() != -1;
    }

    // 获取选中的答案文本
    private String getSelectedAnswer(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        RadioButton radioButton = group.findViewById(selectedId);
        return radioButton.getText().toString();
    }
}
