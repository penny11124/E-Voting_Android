# Ureka_Java_Android
The Java version of Ureka framework.

<details><summary>git小抄</summary>

1. 一開始先記得checkout自己所在branch, 一定要跳回main再新創branch
```git branch <new_branch_name>```
2. 切換到新創的那個branch
```git checkout <new_branch_name>```
3. 才create new project寫new code
4. push到新的branch
```git add .```
```git commit -m 'description'```
```git push -u <new_branch_name>```
5. 在github遠端庫上pull request, merge自己的branch到main後在本地端切回main
```git pull```
6. 重複step 1-5

### 其他指令
* ```git branch``` 看現在有哪些branch
* ```git status``` 看現在有trace哪些file
* ```git branch -d branch_name``` 刪除branch