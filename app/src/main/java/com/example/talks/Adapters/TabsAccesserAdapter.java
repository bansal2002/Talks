package com.example.talks.Adapters;

import com.example.talks.ChatsFragment;
import com.example.talks.ContextsFragment;
import com.example.talks.GroupsFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccesserAdapter extends FragmentPagerAdapter {

    public TabsAccesserAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            case 1:
                GroupsFragment groupsFragment =new GroupsFragment();
                return groupsFragment;
            case 2:
                ContextsFragment contextsFragment =new ContextsFragment();
                return contextsFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
        }
        return null;
    }
}
